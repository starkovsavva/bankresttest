package com.example.bankcards.dto.requests;


import com.example.bankcards.entity.BankCardStatus;

import javax.validation.constraints.NotNull;

public record CardUpdateRequest(
        @NotNull(message = "Status is required")
        BankCardStatus status
) {}