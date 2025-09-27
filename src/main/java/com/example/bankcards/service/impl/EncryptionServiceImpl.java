package com.example.bankcards.service.impl;


import com.example.bankcards.service.EncryptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class EncryptionServiceImpl implements EncryptionService {

    private String secretKey;
    public EncryptionServiceImpl(@Value("${app.encryption.secret}")String secretKey) {
        this.secretKey = secretKey;
    }


    @Override
    public String encrypt(String cardNumber) {
        try {
            // Простое XOR шифрование (для демо)
            byte[] bytes = cardNumber.getBytes(StandardCharsets.UTF_8);
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

            byte[] result = new byte[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                result[i] = (byte) (bytes[i] ^ keyBytes[i % keyBytes.length]);
            }

            return Base64.getEncoder().encodeToString(result);

        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public String decrypt(String encryptedCardNumber) {
        try {
            byte[] bytes = Base64.getDecoder().decode(encryptedCardNumber);
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

            byte[] result = new byte[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                result[i] = (byte) (bytes[i] ^ keyBytes[i % keyBytes.length]);
            }

            return new String(result, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    @Override
    public String hash(String cardNumber) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((cardNumber + secretKey).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }


}
