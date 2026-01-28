package com.example.bankcards.service;

public interface EncryptionService {

    /**
     * Шифрует номер карты для безопасного хранения в БД
     */
    String encrypt(String cardNumber);

    /**
     * Дешифрует номер карты (для внутреннего использования)
     */
    String decrypt(String encryptedCardNumber);

    /**
     * Создает хэш для проверки уникальности номера карты
     */
    String hash(String cardNumber);

}