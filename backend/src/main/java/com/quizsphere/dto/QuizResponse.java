package com.quizsphere.dto;

public record QuizResponse(
        Long id,
        String title,
        String description,
        String difficulty,
        Integer durationMinutes,
        Integer passPercentage,
        boolean published,
        Long categoryId,
        String categoryName,
        int questionCount
) {}
