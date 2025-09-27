package com.example.bankcards.dto;


import com.example.bankcards.entity.BankCardStatus;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for {@link com.example.bankcards.entity.BankCard}
 */
public record BankCardDTO(
        Long id,

        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
        String cardNumber,

        @NotBlank(message = "Card holder name is required")
        String cardHolderName,

        @NotNull(message = "Expiration date is required")
        LocalDateTime expirationDate,

        @NotNull(message = "Status is required")
        BankCardStatus status,

        @NotNull(message = "Balance is required")
        @PositiveOrZero(message = "Balance must be positive or zero")
        BigDecimal balance,

        String maskedCardNumber,

        @NotNull(message = "User ID is required")
        Long userId
) {
    // Статический factory метод для создания с маскированным номером
    public static BankCardDTO of(Long id, String cardNumber, String cardHolderName,
                                 LocalDateTime expirationDate, BankCardStatus status,
                                 BigDecimal balance, Long userId) {
        String maskedNumber = maskCardNumber(cardNumber);
        return new BankCardDTO(id, cardNumber, cardHolderName, expirationDate,
                status, balance, maskedNumber, userId);
    }

    // Для создания с уже замаскированным номером (из базы)
    public static BankCardDTO withMaskedNumber(Long id, String maskedCardNumber,
                                               String cardHolderName, LocalDateTime expirationDate,
                                               BankCardStatus status, BigDecimal balance, Long userId) {
        return new BankCardDTO(id, null, cardHolderName, expirationDate,
                status, balance, maskedCardNumber, userId);
    }

    private static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "**** **** **** ****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}