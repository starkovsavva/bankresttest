package com.example.bankcards.entity;

import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(User.class)
public abstract class User_ {

	public static volatile SingularAttribute<User, String> firstName;
	public static volatile SingularAttribute<User, String> lastName;
	public static volatile SingularAttribute<User, LocalDateTime> createdAt;
	public static volatile SingularAttribute<User, String> password;
	public static volatile SingularAttribute<User, Role> role;
	public static volatile ListAttribute<User, BankCard> cards;
	public static volatile SingularAttribute<User, Long> id;
	public static volatile SingularAttribute<User, String> email;
	public static volatile SingularAttribute<User, String> username;
	public static volatile SingularAttribute<User, LocalDateTime> updatedAt;

	public static final String FIRST_NAME = "firstName";
	public static final String LAST_NAME = "lastName";
	public static final String CREATED_AT = "createdAt";
	public static final String PASSWORD = "password";
	public static final String ROLE = "role";
	public static final String CARDS = "cards";
	public static final String ID = "id";
	public static final String EMAIL = "email";
	public static final String USERNAME = "username";
	public static final String UPDATED_AT = "updatedAt";

}

