package com.example.bankcards.service.criteria;

import com.example.bankcards.entity.BankCardStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Класс критериев для фильтрации сущностей BankCard.
 * Содержит все фильтры для запросов по банковским картам.
 */
@Getter
@Setter
public class BankCardCriteria implements Criteria {

    private static final long serialVersionUID = 1L;

    private Filter<Long> id;
    private Filter<String> cardHolderName;
    private Filter<BankCardStatus> status;
    private Filter<BigDecimal> balance;
    private Filter<LocalDateTime> expirationDate;
    private Filter<Long> userId;
    private Filter<LocalDateTime> createdAt;

    public BankCardCriteria() {
    }

    public BankCardCriteria(BankCardCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.cardHolderName = other.cardHolderName == null ? null : other.cardHolderName.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.balance = other.balance == null ? null : other.balance.copy();
        this.expirationDate = other.expirationDate == null ? null : other.expirationDate.copy();
        this.userId = other.userId == null ? null : other.userId.copy();
        this.createdAt = other.createdAt == null ? null : other.createdAt.copy();
    }

    @Override
    public BankCardCriteria copy() {
        return new BankCardCriteria(this);
    }

    @Override
    public boolean isEmpty() {
        return (id == null || id.isEmpty()) &&
                (cardHolderName == null || cardHolderName.isEmpty()) &&
                (status == null || status.isEmpty()) &&
                (balance == null || balance.isEmpty()) &&
                (expirationDate == null || expirationDate.isEmpty()) &&
                (userId == null || userId.isEmpty()) &&
                (createdAt == null || createdAt.isEmpty());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BankCardCriteria)) return false;
        BankCardCriteria that = (BankCardCriteria) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(cardHolderName, that.cardHolderName) &&
                Objects.equals(status, that.status) &&
                Objects.equals(balance, that.balance) &&
                Objects.equals(expirationDate, that.expirationDate) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cardHolderName, status, balance, expirationDate, userId, createdAt);
    }

    @Override
    public String toString() {
        return "BankCardCriteria{" +
                "id=" + id +
                ", cardHolderName=" + cardHolderName +
                ", status=" + status +
                ", balance=" + balance +
                ", expirationDate=" + expirationDate +
                ", userId=" + userId +
                ", createdAt=" + createdAt +
                '}';
    }
}