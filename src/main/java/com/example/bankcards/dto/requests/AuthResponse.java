package com.example.bankcards.dto.requests;

public record AuthResponse(
        String token,
        String username,
        String role,
        Long userId
) {
    public static AuthResponse of(String token, String username, String role, Long userId) {
        return new AuthResponse(token, username, role, userId);
    }
}