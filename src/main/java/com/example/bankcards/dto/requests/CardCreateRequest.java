package com.example.bankcards.dto.requests;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

public record CardCreateRequest(
        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
        String cardNumber,

        @NotBlank(message = "Card holder name is required")
        String cardHolderName,

        @NotNull(message = "Expiration date is required")
        LocalDateTime expirationDate,

        Long targetUserId  // Опционально: для админа, чтобы создать карту другому пользователю
) {}