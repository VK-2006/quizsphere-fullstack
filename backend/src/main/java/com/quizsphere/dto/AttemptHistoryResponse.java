package com.quizsphere.dto;

import java.time.Instant;

public record AttemptHistoryResponse(
        Long attemptId,
        Long quizId,
        String quizTitle,
        String category,
        Integer score,
        Integer totalMarks,
        Double percentage,
        boolean passed,
        String status,
        Instant startedAt,
        Instant submittedAt
) {}
