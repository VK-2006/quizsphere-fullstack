package com.quizsphere.dto;

public record AuthResponse(
        String token, Long userId, String fullName, String email, String role,
        String avatarUrl, String authProvider
) {}
