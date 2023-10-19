package ru.aston.bankapi.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.aston.bankapi.dto.AccountDto;
import ru.aston.bankapi.dto.PaymentDto;
import ru.aston.bankapi.exceptionHandler.InvalidDataException;
import ru.aston.bankapi.exceptionHandler.NotEnoughFundsException;
import ru.aston.bankapi.exceptionHandler.NotFoundException;
import ru.aston.bankapi.model.Account;
import ru.aston.bankapi.service.AccountService;
import ru.aston.bankapi.service.AccountServiceImpl;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
public class ApiAccountController {

    private final AccountService accountService;

    public ApiAccountController(AccountServiceImpl accountService) {
        this.accountService = accountService;
    }

    @PostMapping()
    public ResponseEntity<String> saveAccount(@RequestBody AccountDto accountJson) {

        String name = accountJson.getName();
        String pinCode = accountJson.getPinCode();

        try {
            accountService.createAccount(name, pinCode);
        } catch (InvalidDataException e) {
            log.error("Невалидные данные", e);
            return new ResponseEntity<>("Данные введены некорректно", HttpStatus.BAD_REQUEST);
        }
        log.info("Новый аккаунт создан!");
        return new ResponseEntity<>("Новый аккаунт создан", HttpStatus.OK);

    }

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {

        var allAccounts = accountService.getAllAccounts();

        return new ResponseEntity<>(allAccounts, HttpStatus.OK);
    }

    @GetMapping(value = "/{accNum}")
    public ResponseEntity<Account> getAccount(@PathVariable @NotBlank String accNum) {
        var accountByAccNum = accountService.getAccountByAccNum(accNum);
        if(accountByAccNum.isPresent()){
            return new ResponseEntity<>(accountByAccNum.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PatchMapping(value = "/{accNum}/deposit")
    public ResponseEntity<String> deposit(@PathVariable @NotBlank String accNum, @RequestBody PaymentDto paymentJson) {

        var amount = paymentJson.getAmountOfOperation();

        try {
            accountService.deposit(accNum, amount);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>("Перевод выполнен", HttpStatus.OK);
    }

    @PatchMapping(value = "/{fromAccNum}/transfer/{toAccNum}")
    public ResponseEntity<String> transfer(@PathVariable @NotBlank String fromAccNum,
                                           @PathVariable @NotBlank String toAccNum,
                                           @RequestBody PaymentDto paymentJson) {

        var amount = paymentJson.getAmountOfOperation();
        var pinCode = paymentJson.getPinCode();

        try {
            accountService.transfer(fromAccNum, toAccNum, amount, pinCode);

        } catch (NotFoundException e) {
            return new ResponseEntity<>("Ошибка перевода", HttpStatus.BAD_REQUEST);
        } catch (InvalidDataException e) {
            return new ResponseEntity<>("Неверный пин", HttpStatus.BAD_REQUEST);
        } catch (NotEnoughFundsException e) {
            return new ResponseEntity<>("Недостаточно средств", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Перевод выполнен успешно", HttpStatus.OK);

    }

    @PatchMapping(value = "/{accNum}/withdraw")
    public ResponseEntity<String> withdraw(@PathVariable @NotBlank String accNum,
                                           @RequestBody PaymentDto paymentJson) {

        var amount = paymentJson.getAmountOfOperation();
        var pinCode = paymentJson.getPinCode();

        try {
            accountService.withdraw(accNum, amount, pinCode);

        } catch (NotFoundException e) {
            return new ResponseEntity<>("Ошибка перевода", HttpStatus.BAD_REQUEST);
        } catch (NotEnoughFundsException e) {
            return new ResponseEntity<>("Недостаточно средств", HttpStatus.BAD_REQUEST);
        } catch (InvalidDataException e) {
            return new ResponseEntity<>("Неверный пин", HttpStatus.BAD_REQUEST);

        }

        return new ResponseEntity<>("Перевод выполнен успешно", HttpStatus.OK);


    }


}