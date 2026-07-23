package com.quizsphere.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecoverySettingsRequest(
        String currentPassword,
        @NotBlank @Size(min = 5, max = 255) String securityQuestion,
        @NotBlank @Size(min = 2, max = 100) String securityAnswer
) {}
