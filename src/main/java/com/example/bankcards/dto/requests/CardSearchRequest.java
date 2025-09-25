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
        // default
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;
    }


    public static CardSearchRequest defaultRequest() {
        return new CardSearchRequest(null, null, null, null, 0, 20);
    }
}