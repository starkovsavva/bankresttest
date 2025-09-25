package com.example.bankcards.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message, String entity, String param) {
        super(String.format("%s %s %s", message, entity, param));
    }
}