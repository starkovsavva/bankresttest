package com.example.bankcards.service.mapper;


import com.example.bankcards.dto.BankCardDTO;
import com.example.bankcards.entity.BankCard;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BankCardMapper extends EntityMapper<BankCardDTO, BankCard> {
    @Mapping(target = "maskedCardNumber", source = "cardNumber", qualifiedByName = "maskCardNumber")
    @Mapping(target = "userId", source = "user.id")
    BankCardDTO toDto(BankCard bankCard);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cardNumberHash", ignore = true)
    BankCard toEntity(BankCardDTO bankCardDTO);

    @Named("maskCardNumber")
    default String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "**** **** **** ****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    @Named("bankCardSummary")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "maskedCardNumber", source = "cardNumber", qualifiedByName = "maskCardNumber")
    @Mapping(target = "cardHolderName", source = "cardHolderName")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "userId", source = "user.id")
    BankCardDTO toDtoSummary(BankCard bankCard);

    @Named("toDtoSummaryPage")
    default Page<BankCardDTO> toDtoSummaryPage(Page<BankCard> bankCards) {
        return bankCards.map(this::toDtoSummary);
    }
}
