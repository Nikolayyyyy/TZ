package ru.aston.bankapi.service;


import ru.aston.bankapi.model.Account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountService {
    Optional<Account> createAccount(String name, String pinCode);
    List<Account> getAllAccounts();
    Optional<Account> getAccountByAccNum(String accNum);
    void deposit (String accNum, BigDecimal amount);

    void transfer(String fromAccNum, String toAccNum, BigDecimal amount, String pinCode);

    void withdraw(String accNum, BigDecimal amount, String pinCode);
}
