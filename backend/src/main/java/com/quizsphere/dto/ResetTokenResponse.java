package com.quizsphere.dto;

public record ResetTokenResponse(
        String message,
        String resetToken,
        long expiresInMinutes
) {}
