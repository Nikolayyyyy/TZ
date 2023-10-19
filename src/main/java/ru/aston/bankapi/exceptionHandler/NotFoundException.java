package ru.aston.bankapi.exceptionHandler;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String string) {
        super(string);
    }
}
