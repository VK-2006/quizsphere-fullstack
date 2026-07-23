package com.quizsphere.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SecurityAnswerRequest(
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 2, max = 100) String securityAnswer
) {}
