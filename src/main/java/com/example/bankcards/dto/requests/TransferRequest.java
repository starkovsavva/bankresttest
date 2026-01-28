package com.example.bankcards.dto.requests;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransferRequest(
        @NotNull(message = "Source card ID is required")
        Long fromCardId,

        @NotNull(message = "Destination card ID is required")
        Long toCardId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount
) {
    public TransferRequest {
        // Compact constructor для дополнительной валидации
        if (fromCardId != null && toCardId != null && fromCardId.equals(toCardId)) {
            throw new IllegalArgumentException("Cannot transfer to the same card");
        }
    }
}