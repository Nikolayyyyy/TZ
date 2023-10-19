package ru.aston.bankapi.service;

import ru.aston.bankapi.model.Transaction;

import java.util.List;

public interface TransactionService {

    List<Transaction> getAllTransactions();
    List<Transaction> getAllTransactionsByAccNum(String accNum);
}
