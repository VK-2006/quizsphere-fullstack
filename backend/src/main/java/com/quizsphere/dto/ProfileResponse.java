package com.quizsphere.dto;

import java.time.Instant;
import java.time.LocalDate;

public record ProfileResponse(
        Long id, String fullName, String email, String role, boolean enabled,
        String authProvider, String avatarUrl, String bio, String phone, String location,
        LocalDate dateOfBirth, Instant joinedAt, long totalAttempts, long passedAttempts, double averageScore
) {}
