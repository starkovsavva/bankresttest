package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class CardAccessDeniedException extends ApiException {

    public CardAccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN, "CARD_ACCESS_DENIED");
    }

    public CardAccessDeniedException() {
        this("Access to this card is denied");
    }
}
