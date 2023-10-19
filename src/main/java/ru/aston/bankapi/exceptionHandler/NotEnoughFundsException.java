package ru.aston.bankapi.exceptionHandler;

public class NotEnoughFundsException extends RuntimeException {
    public NotEnoughFundsException() {
        super("Недостаточно средств на счете.");
    }
}