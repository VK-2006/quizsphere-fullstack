package com.quizsphere.dto;

public record RecoveryChallengeResponse(
        String message,
        String challengeToken,
        long expiresInMinutes
) {}
