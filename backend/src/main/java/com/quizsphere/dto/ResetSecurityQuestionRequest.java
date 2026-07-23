package com.quizsphere.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetSecurityQuestionRequest(
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 40) String recoveryCode,
        @NotBlank @Size(min = 5, max = 255) String securityQuestion,
        @NotBlank @Size(min = 2, max = 100) String securityAnswer
) {}
