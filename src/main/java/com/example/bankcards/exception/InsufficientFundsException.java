package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class InsufficientFundsException extends ApiException {

    public InsufficientFundsException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INSUFFICIENT_FUNDS");
    }

    public InsufficientFundsException() {
        this("Insufficient funds for this operation");
    }
}
