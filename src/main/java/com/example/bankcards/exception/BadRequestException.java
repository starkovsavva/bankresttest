package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {

    private final String entityName;
    private final String errorKey;

    public BadRequestException(String message, String entityName, String errorKey) {
        super(message, HttpStatus.BAD_REQUEST, errorKey);
        this.entityName = entityName;
        this.errorKey = errorKey;
    }

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
        this.entityName = null;
        this.errorKey = "BAD_REQUEST";
    }

    public String getEntityName() {
        return entityName;
    }

    public String getErrorKey() {
        return errorKey;
    }
}