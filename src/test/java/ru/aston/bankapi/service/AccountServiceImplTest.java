package ru.aston.bankapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    public void createAccount_WithValidValues_returnsCreatedAccount() {
        String name = "Nikolay";
        String pinCode = "4321";

        Account account = new Account(name, pinCode);
        Mockito.doReturn(account).when(accountRepository).save(any(Account.class));

        Optional<Account> createdAccount = accountService.createAccount(name, pinCode);

        assertTrue(createdAccount.isPresent());
        assertEquals(createdAccount.get().getName(), name);
        assertEquals(createdAccount.get().getPinCode(), pinCode);

    }

    @Test
    public void createAccount_WithInvalidValues_throwsInvalidPinCodeException() {
        assertThrows(InvalidDataException.class, () -> accountService.createAccount(null, "555"));
        assertThrows(InvalidDataException.class, () -> accountService.createAccount("Ivan", "3"));
    }

    @Test
    void getAllAccounts_ReturnsAllAccountsFromRepository() {
        var account = new Account("Nikolay", "4321");
        var account1 = new Account("Viktor", "1234");

        var list = Arrays.asList(account, account1);

        when(accountRepository.findAll()).thenReturn(list);

        var result = accountService.getAllAccounts();

        assertEquals(list, result);
    }

    @Test
    void getAccountByAccNum_WithValidAccNum_ReturnsAccount() {
        var accNum = "6ce5e8d1-61af-4b92-9fa8-b2a2466c9bc4";
        var account = new Account(accNum, "Petr", "1234", BigDecimal.ZERO);

        when(accountRepository.findById(accNum)).thenReturn(Optional.of(account));

        var result = accountService.getAccountByAccNum("6ce5e8d1-61af-4b92-9fa8-b2a2466c9bc4");

        assertTrue(result.isPresent());
        assertEquals(result.get(), account);
    }

    @Test
    public void deposit_WithValidAccNum_DepositsAmountAndSavesTransaction() {
        var accNum = "4587fcc0-f406-4f45-9c67-30c7fa02e30f";
        var amountToDeposit = new BigDecimal("25.00");
        var existingAccount = new Account(accNum, "Viktor Larionov", "3333", new BigDecimal("230.00"));
        var transaction = new Transaction(accNum, accNum, LocalTime.now(), amountToDeposit, Operation.DEPOSIT);

        when(accountRepository.findById(accNum)).thenReturn(Optional.of(existingAccount));
        when(transactionRepository.save(any())).thenReturn(transaction);
        // act
        accountService.deposit(accNum, amountToDeposit);

        // assert
        verify(accountRepository).save(existingAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        assertEquals(new BigDecimal("255.00"), existingAccount.getAmount());
    }

    @Test
    void deposit_ThrowsNotFoundException_WhenAccountNotFound() {
        var accNum = "4587fcc0-f406-4f45-9c67-30c7fa02e30f";

        when(accountRepository.findById(accNum)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> accountService.deposit(accNum, BigDecimal.TEN));

        verify(accountRepository, times(1)).findById(accNum);
    }

    @Test
    void deposit_IncreasesAccountBalance() {
        var accNum = "4587fcc0-f406-4f45-9c67-30c7fa02e30f";
        var existingAccount = new Account(accNum, "Ivan Ivanovich", "3210", BigDecimal.TEN);


        when(accountRepository.findById(accNum)).thenReturn(Optional.of(existingAccount));

        BigDecimal amount = new BigDecimal(500);
        accountService.deposit(accNum, amount);

        BigDecimal expectedBalance = BigDecimal.valueOf(510);
        assertEquals(expectedBalance, existingAccount.getAmount());

        verify(accountRepository, times(1)).save(existingAccount);
    }

    @Test
    void deposit_CreatesTransaction() {
        var accNum = "4587fcc0-f406-4f45-9c67-30c7fa02e30f";
        var existingAccount = new Account(accNum, "Ivan Ivanovich", "3535", BigDecimal.TEN);

        when(accountRepository.findById(accNum)).thenReturn(Optional.of(existingAccount));

        var amount = new BigDecimal(500);
        accountService.deposit(accNum, amount);

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void transferSuccess() {
        var fromAccNum = "4587fcc0-f406-4f45-9c67-30c7fa02e30f";
        var fromAccount = new Account(fromAccNum, "Ivan Ivanovich", "3210", BigDecimal.TEN);
        var accNum = "4587fcc0-f406-4f45-9c67-30c7fa02e555";
        var toAccount = new Account(accNum, "Ivan Ivanovich", "4321", BigDecimal.ZERO);

        when(accountRepository.findById(fromAccNum)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(accNum)).thenReturn(Optional.of(toAccount));

        accountService.transfer(fromAccNum, accNum, BigDecimal.TEN, "3210");

        assertEquals(BigDecimal.ZERO, fromAccount.getAmount());
        assertEquals(BigDecimal.TEN, toAccount.getAmount());
    }

    @Test
    void transfer_CreatesTransaction() {
        var fromAccNum = "4587fcc0-f406-4f45-9c67-30c7fa02e30f";
        var fromAccount = new Account(fromAccNum, "Ivan Ivanovich", "3210", BigDecimal.TEN);
        var toAccNum = "4587fcc0-f406-4f45-9c67-30c7fa02e4ee";
        var toAccount = new Account(toAccNum, "Ivan Ivanovich", "4321", BigDecimal.ZERO);

        when(accountRepository.findById(fromAccNum)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccNum)).thenReturn(Optional.of(toAccount));

        accountService.transfer(fromAccNum, toAccNum, BigDecimal.TEN, "3210");

        verify(transactionRepository, times(1)).save(any(Transaction.class));

    }

    @Test()
    public void transferNotEnoughFunds() {
        var fromAccNum = "4587fcc0-f406-4f45-9c67-30c7fa02e30f";
        var fromAccount = new Account(fromAccNum, "Ivan Ivanovich", "3210", BigDecimal.TEN);
        var toAccNum = "4587fcc0-f406-4f45-9c67-30c7fa02e4ee";
        var toAccount = new Account(toAccNum, "Ivan Ivanovich", "4321", BigDecimal.ZERO);

        when(accountRepository.findById(fromAccNum)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccNum)).thenReturn(Optional.of(toAccount));

        assertThrows(NotEnoughFundsException.class, () -> accountService.transfer(fromAccNum, toAccNum, BigDecimal.valueOf(20), "3210"));
    }


    @Test
    public void transferAccountNotFound() {
        var fromAccNum = "4587fcc0-f406-4f45-9c67-30c7fa02e30f";
        var fromAccount = new Account(fromAccNum, "Ivan Ivanovich", "3210", BigDecimal.TEN);
        String toAccNum = "4587fcc0-f406-4f45-9c67-30c7fa02e4ee";

        when(accountRepository.findById(fromAccNum)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccNum)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                accountService.transfer(fromAccNum, toAccNum, BigDecimal.TEN, "3210"));
    }

    @Test
    public void testWithdrawSuccess() {
        var fromAccNum = "4587fcc0-f406-4f45-9c67-30c7fa02e30f";
        var fromAccount = new Account(fromAccNum, "Ivan Ivanovich", "3210", BigDecimal.TEN);

        when(accountRepository.findById(fromAccNum)).thenReturn(Optional.of(fromAccount));

        accountService.withdraw(fromAccNum, BigDecimal.TEN, fromAccount.getPinCode());

        assertEquals(BigDecimal.ZERO, fromAccount.getAmount());
    }

    @Test
    public void testWithdrawNotFoundException() {
        var accNum = "55555";
        var amount = new BigDecimal(1000);
        var pinCode = "0000";

        assertThrows(NotFoundException.class, () ->
            accountService.withdraw(accNum, amount, pinCode)
        );
    }

    @Test
    public void testWithdrawNotEnoughFundsException() {
        var fromAccNum = "4587fcc0-f406-4f45-9c67-30c7fa02e30f";
        var fromAccount = new Account(fromAccNum, "Ivan Ivanovich", "3210", BigDecimal.TEN);

        when(accountRepository.findById(fromAccNum)).thenReturn(Optional.of(fromAccount));

        assertThrows(NotEnoughFundsException.class, () ->
            accountService.withdraw(fromAccNum, BigDecimal.valueOf(55), fromAccount.getPinCode())
        );
    }

    @Test
    public void testWithdrawInvalidPinCodeException() {
        var fromAccNum = "4587fcc0-f406-4f45-9c67-30c7fa02e30f";
        var fromAccount = new Account(fromAccNum, "Ivan Ivanovich", "3210", BigDecimal.TEN);

        when(accountRepository.findById(fromAccNum)).thenReturn(Optional.of(fromAccount));

        assertThrows(InvalidDataException.class, () ->
            accountService.withdraw(fromAccNum, BigDecimal.TEN, "5")
        );
    }
}