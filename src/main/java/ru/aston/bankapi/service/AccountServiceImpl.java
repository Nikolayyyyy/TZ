package ru.aston.bankapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aston.bankapi.exceptionHandler.InvalidDataException;
import ru.aston.bankapi.exceptionHandler.NotEnoughFundsException;
import ru.aston.bankapi.exceptionHandler.NotFoundException;
import ru.aston.bankapi.model.Account;
import ru.aston.bankapi.model.Operation;
import ru.aston.bankapi.model.Transaction;
import ru.aston.bankapi.repository.AccountRepository;
import ru.aston.bankapi.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public Optional<Account> createAccount(String name, String pinCode) {
        log.info("Вызван метод createAccount с параметрами name={}, pinCode={}", name, "*".repeat(pinCode.length()));

        if ((name == null) || (pinCode.length() != 4)) {
            log.error("Введены некорректные данные");
            throw new InvalidDataException("Введены некорректные данные");
        }

        var account = new Account(name, pinCode);

        accountRepository.save(account);

        log.info("Учетная запись успешно создана: name={}, pinCode={}", name, "*".repeat(pinCode.length()));

        return Optional.of(account);
    }

    @Override
    public List<Account> getAllAccounts() {
        log.info("Вызван метод getAllAccounts");

        List<Account> accounts = accountRepository.findAll();

        log.info("Найдено {} аккаунтов", accounts.size());

        return accounts;
    }

    @Override
    public Optional<Account> getAccountByAccNum(String accNum) {
        log.info("Вызван метод getAccountByAccNum с параметром {}", accNum);

        Optional<Account> account = accountRepository.findById(accNum);

        if (account.isEmpty()) {
            log.error("Аккаунт с номером {} не найден", accNum);
        } else {
            log.info("Найден аккаунт: {}", account.get());
        }

        return account;
    }

    @Override
    @Transactional
    public void deposit(String accNum, BigDecimal amount) {
        log.info("Вызван метод deposit с параметрами {}, {}", accNum, amount);

        Optional<Account> byAccountId = accountRepository.findById(accNum);

        if (byAccountId.isEmpty()) {
            log.error("Аккаунт с номером {} не найден", accNum);
            throw new NotFoundException("Аккаунт с данным номером не найден");
        }

        Account account = byAccountId.get();
        Transaction transaction = new Transaction(accNum, accNum, LocalTime.now(), amount, Operation.DEPOSIT);

        account.setAmount(account.getAmount().add(amount));

        accountRepository.save(account);
        transactionRepository.save(transaction);

        log.info("Сумма {} успешно зачислена на аккаунт {}", amount, accNum);
    }

    @Override
    @Transactional
    public void transfer(String fromAccNum, String toAccNum, BigDecimal amount, String pinCode) {
        log.info("Вызван метод transfer с параметрами {}, {}",
                fromAccNum, toAccNum);

        var fromAccount = accountRepository.findById(fromAccNum);
        var toAccount = accountRepository.findById(toAccNum);

        if ((fromAccount.isEmpty()) || (toAccount.isEmpty())) {
            log.error("Ошибка! Перевод не выполнен!");
            throw new NotFoundException("Ошибка! Перевод не выполнен!");
        }
        if (!(fromAccount.get().getPinCode().equals(pinCode))) {
            log.error("Пин код введен неверно!");
            throw new InvalidDataException("Пин код введен неверно!");
        }
        if (fromAccount.get().getAmount().compareTo(amount) < 0) {
            log.error("Недостаточно средств на счете!");
            throw new NotEnoughFundsException();
        }

        fromAccount.get().setAmount(fromAccount.get().getAmount().subtract(amount));
        toAccount.get().setAmount(toAccount.get().getAmount().add(amount));

        var transaction = new Transaction(fromAccNum, toAccNum, LocalTime.now(), amount, Operation.TRANSFER);

        accountRepository.save(fromAccount.get());
        accountRepository.save(toAccount.get());
        transactionRepository.save(transaction);

        log.info("Перевод успешно выполнен");
    }

    @Override
    @Transactional
    public void withdraw(String accNum, BigDecimal amount, String pinCode) {
        log.info("Вызван метод withdraw с параметрами {}, {}",
                accNum, amount);

        var byAccountId = accountRepository.findById(accNum);

        if (byAccountId.isEmpty()) {
            log.error("Аккаунта с таким номером - {} не найдено", accNum);
            throw new NotFoundException("Аккаунта с таким номером не существует");
        }
        if (byAccountId.get().getAmount().compareTo(amount) < 0) {
            log.error("Недостаточно средств для данной операции");
            throw new NotEnoughFundsException();
        }
        if (!(byAccountId.get().getPinCode().equals(pinCode))) {
            log.error("Пин-код введен неверно!");
            throw new InvalidDataException("Пин код вееден неверно!");
        }

        var account = byAccountId.get();
        var transaction = new Transaction(accNum, accNum, LocalTime.now(), amount, Operation.WITHDRAW);

        account.setAmount(account.getAmount().subtract(amount));

        accountRepository.save(account);
        transactionRepository.save(transaction);
        log.info("Операция по снятию денежных средств успешно выполнена!");
    }

}
