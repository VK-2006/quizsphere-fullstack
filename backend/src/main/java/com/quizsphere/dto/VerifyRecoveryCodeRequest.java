package com.quizsphere.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyRecoveryCodeRequest(
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank String challengeToken,
        @NotBlank @Size(min = 8, max = 40) String recoveryCode
) {}
