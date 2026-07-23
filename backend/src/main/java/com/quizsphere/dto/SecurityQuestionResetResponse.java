package com.quizsphere.dto;

public record SecurityQuestionResetResponse(
        String message,
        String resetToken,
        long expiresInMinutes,
        String recoveryCode
) {}
