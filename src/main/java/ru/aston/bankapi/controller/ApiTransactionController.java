package ru.aston.bankapi.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.aston.bankapi.model.Transaction;
import ru.aston.bankapi.service.TransactionService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/version1/transactions")
@RequiredArgsConstructor
public class ApiTransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        var allTransactions = transactionService.getAllTransactions();

        return new ResponseEntity<>(allTransactions, HttpStatus.OK);
    }

    @GetMapping(value = "/{accNum}")
    public ResponseEntity<List<Transaction>> getAllTransactionsById(@PathVariable @NotBlank String accNum) {

        var allTransactionsByAccNum = transactionService.getAllTransactionsByAccNum(accNum);

        return new ResponseEntity<>(allTransactionsByAccNum, HttpStatus.OK);
    }
}
