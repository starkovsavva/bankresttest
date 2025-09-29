package com.example.bankcards.service.mapper;

import com.example.bankcards.dto.BankCardDTO;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.BankCardStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-29T22:51:05+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class BankCardMapperImpl implements BankCardMapper {

    @Override
    public List<BankCard> toEntity(List<BankCardDTO> dtoList) {
        if ( dtoList == null ) {
            return null;
        }

        List<BankCard> list = new ArrayList<BankCard>( dtoList.size() );
        for ( BankCardDTO bankCardDTO : dtoList ) {
            list.add( toEntity( bankCardDTO ) );
        }

        return list;
    }

    @Override
    public List<BankCardDTO> toDto(List<BankCard> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<BankCardDTO> list = new ArrayList<BankCardDTO>( entityList.size() );
        for ( BankCard bankCard : entityList ) {
            list.add( toDto( bankCard ) );
        }

        return list;
    }

    @Override
    public void partialUpdate(BankCard entity, BankCardDTO dto) {
        if ( dto == null ) {
            return;
        }

        if ( dto.id() != null ) {
            entity.setId( dto.id() );
        }
        if ( dto.cardNumber() != null ) {
            entity.setCardNumber( dto.cardNumber() );
        }
        if ( dto.cardHolderName() != null ) {
            entity.setCardHolderName( dto.cardHolderName() );
        }
        if ( dto.expirationDate() != null ) {
            entity.setExpirationDate( dto.expirationDate() );
        }
        if ( dto.status() != null ) {
            entity.setStatus( dto.status() );
        }
        if ( dto.balance() != null ) {
            entity.setBalance( dto.balance() );
        }
    }

    @Override
    public BankCardDTO toDto(BankCard bankCard) {
        if ( bankCard == null ) {
            return null;
        }

        String maskedCardNumber = null;
        Long userId = null;
        Long id = null;
        String cardNumber = null;
        String cardHolderName = null;
        LocalDateTime expirationDate = null;
        BankCardStatus status = null;
        BigDecimal balance = null;

        maskedCardNumber = maskCardNumber( bankCard.getCardNumber() );
        userId = bankCard.getId();
        id = bankCard.getId();
        cardNumber = bankCard.getCardNumber();
        cardHolderName = bankCard.getCardHolderName();
        expirationDate = bankCard.getExpirationDate();
        status = bankCard.getStatus();
        balance = bankCard.getBalance();

        BankCardDTO bankCardDTO = new BankCardDTO( id, cardNumber, cardHolderName, expirationDate, status, balance, maskedCardNumber, userId );

        return bankCardDTO;
    }

    @Override
    public BankCard toEntity(BankCardDTO bankCardDTO) {
        if ( bankCardDTO == null ) {
            return null;
        }

        BankCard bankCard = new BankCard();

        bankCard.setId( bankCardDTO.id() );
        bankCard.setCardNumber( bankCardDTO.cardNumber() );
        bankCard.setCardHolderName( bankCardDTO.cardHolderName() );
        bankCard.setExpirationDate( bankCardDTO.expirationDate() );
        bankCard.setStatus( bankCardDTO.status() );
        bankCard.setBalance( bankCardDTO.balance() );

        return bankCard;
    }

    @Override
    public BankCardDTO toDtoSummary(BankCard bankCard) {
        if ( bankCard == null ) {
            return null;
        }

        Long id = null;
        String maskedCardNumber = null;
        String cardHolderName = null;
        BigDecimal balance = null;
        BankCardStatus status = null;
        String cardNumber = null;
        LocalDateTime expirationDate = null;

        id = bankCard.getId();
        maskedCardNumber = maskCardNumber( bankCard.getCardNumber() );
        cardHolderName = bankCard.getCardHolderName();
        balance = bankCard.getBalance();
        status = bankCard.getStatus();
        cardNumber = bankCard.getCardNumber();
        expirationDate = bankCard.getExpirationDate();

        Long userId = null;

        BankCardDTO bankCardDTO = new BankCardDTO( id, cardNumber, cardHolderName, expirationDate, status, balance, maskedCardNumber, userId );

        return bankCardDTO;
    }
}
