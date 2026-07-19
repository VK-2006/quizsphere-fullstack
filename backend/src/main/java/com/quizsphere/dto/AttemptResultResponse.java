package com.quizsphere.dto;

import java.time.Instant;

public record AttemptResultResponse(
        Long attemptId,
        Long quizId,
        String quizTitle,
        Integer score,
        Integer totalMarks,
        Double percentage,
        boolean passed,
        String status,
        Instant startedAt,
        Instant submittedAt
) {}
