package com.quizsphere.dto;

import com.quizsphere.entity.Difficulty;
import jakarta.validation.constraints.*;

public record QuizRequest(
        @NotBlank @Size(max = 150) String title,
        @Size(max = 1000) String description,
        @NotNull Difficulty difficulty,
        @NotNull @Min(1) @Max(180) Integer durationMinutes,
        @NotNull @Min(1) @Max(100) Integer passPercentage,
        @NotNull Long categoryId,
        boolean published
) {}
