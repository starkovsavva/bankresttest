package com.example.bankcards.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(BankCard.class)
public abstract class BankCard_ {

	public static volatile SingularAttribute<BankCard, LocalDateTime> createdAt;
	public static volatile SingularAttribute<BankCard, BigDecimal> balance;
	public static volatile SingularAttribute<BankCard, String> cardHolderName;
	public static volatile SingularAttribute<BankCard, Long> id;
	public static volatile SingularAttribute<BankCard, User> user;
	public static volatile SingularAttribute<BankCard, String> cardNumber;
	public static volatile SingularAttribute<BankCard, String> cardNumberHash;
	public static volatile SingularAttribute<BankCard, LocalDateTime> expirationDate;
	public static volatile SingularAttribute<BankCard, BankCardStatus> status;
	public static volatile SingularAttribute<BankCard, LocalDateTime> updatedAt;

	public static final String CREATED_AT = "createdAt";
	public static final String BALANCE = "balance";
	public static final String CARD_HOLDER_NAME = "cardHolderName";
	public static final String ID = "id";
	public static final String USER = "user";
	public static final String CARD_NUMBER = "cardNumber";
	public static final String CARD_NUMBER_HASH = "cardNumberHash";
	public static final String EXPIRATION_DATE = "expirationDate";
	public static final String STATUS = "status";
	public static final String UPDATED_AT = "updatedAt";

}

