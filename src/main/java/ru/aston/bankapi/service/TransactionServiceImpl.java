package ru.aston.bankapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aston.bankapi.model.Transaction;
import ru.aston.bankapi.repository.TransactionRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public List<Transaction> getAllTransactions() {
        log.info("Вызван метод getAllTransactions");
        return transactionRepository.findAll();
    }

    @Override
    public List<Transaction> getAllTransactionsByAccNum(String accNum) {
        log.info("Вызван метод getAllTransactionsByAccNum с параметром accNum={}", accNum);
        return transactionRepository.findTransactionsByAccNumFrom(accNum);
    }
}
