package com.quizsphere.dto;

public record RecoverySettingsResponse(
        boolean configured,
        String question,
        String recoveryCode,
        String message
) {}
