package com.example.bankcards.service.criteria;


import com.example.bankcards.entity.BankCardStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Criteria class for filtering BankCard entities.
 * Contains all the filters for bank card queries.
 */
public class BankCardCriteria {

    private Filter<Long> id;
    private Filter<String> cardHolderName;
    private Filter<BankCardStatus> status;
    private Filter<BigDecimal> balance;
    private Filter<LocalDateTime> expirationDate;
    private Filter<Long> userId;
    private Filter<LocalDateTime> createdAt;

    private static class BankCardStatusFilter extends Filter<BankCardStatus>{
        public BankCardStatusFilter() {}

        public BankCardStatusFilter(BankCardStatusFilter filter) {
            super(filter);
        }
        @Override
        public BankCardStatusFilter copy() {
            return new BankCardStatusFilter(this);
        }
    }
//    public static class BankCard
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

    public BankCardCriteria copy() {
        return new BankCardCriteria(this);
    }

    // Getters and Setters
    public Filter<Long> getId() { return id; }
    public void setId(Filter<Long> id) { this.id = id; }

    public Filter<String> getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(Filter<String> cardHolderName) { this.cardHolderName = cardHolderName; }

    public Filter<BankCardStatus> getStatus() { return status; }
    public void setStatus(Filter<BankCardStatus> status) { this.status = status; }

    public Filter<BigDecimal> getBalance() { return balance; }
    public void setBalance(Filter<BigDecimal> balance) { this.balance = balance; }

    public Filter<LocalDateTime> getExpirationDate() { return expirationDate; }
    public void setExpirationDate(Filter<LocalDateTime> expirationDate) { this.expirationDate = expirationDate; }

    public Filter<Long> getUserId() { return userId; }
    public void setUserId(Filter<Long> userId) { this.userId = userId; }

    public Filter<LocalDateTime> getCreatedAt() { return createdAt; }
    public void setCreatedAt(Filter<LocalDateTime> createdAt) { this.createdAt = createdAt; }

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
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getCardHolderName(), that.getCardHolderName()) &&
                Objects.equals(getStatus(), that.getStatus()) &&
                Objects.equals(getBalance(), that.getBalance()) &&
                Objects.equals(getExpirationDate(), that.getExpirationDate()) &&
                Objects.equals(getUserId(), that.getUserId()) &&
                Objects.equals(getCreatedAt(), that.getCreatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getId(),
                getCardHolderName(),
                getStatus(),
                getBalance(),
                getExpirationDate(),
                getUserId(),
                getCreatedAt()
        );
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