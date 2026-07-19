package com.quizsphere.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequest(
        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        String fullName,
        @Size(max = 500, message = "Bio cannot exceed 500 characters") String bio,
        @Pattern(regexp = "^$|^[0-9+()\\- ]{7,30}$", message = "Enter a valid phone number") String phone,
        @Size(max = 120, message = "Location cannot exceed 120 characters") String location,
        LocalDate dateOfBirth,
        @Size(max = 1000, message = "Avatar URL is too long") String avatarUrl
) {}
