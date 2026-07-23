package com.quizsphere.dto;

public record RecoveryQuestionResponse(
        String question,
        boolean recoveryConfigured
) {}
