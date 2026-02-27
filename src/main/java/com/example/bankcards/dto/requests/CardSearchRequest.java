package com.example.bankcards.dto.requests;


import com.example.bankcards.entity.BankCardStatus;

import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

public record CardSearchRequest(
        String cardHolderName,
        BankCardStatus status,
        LocalDateTime expirationDateFrom,
        LocalDateTime expirationDateTo,

        @Positive(message = "Page must be positive")
        int page,

        @Positive(message = "Size must be positive")
        int size
) {
    public CardSearchRequest {
        if (page < 0) {
            throw new IllegalArgumentException("Page number must not be negative");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
    }


    public static CardSearchRequest defaultRequest() {
        return new CardSearchRequest(null, null, null, null, 0, 20);
    }
}