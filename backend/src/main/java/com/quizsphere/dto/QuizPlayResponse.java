package com.quizsphere.dto;

import java.time.Instant;
import java.util.List;

public record QuizPlayResponse(
        Long attemptId,
        Long quizId,
        String title,
        String description,
        Integer durationMinutes,
        Instant startedAt,
        List<PlayQuestion> questions
) {
    public record PlayQuestion(Long id, String questionText, Integer marks, List<PlayOption> options) {}
    public record PlayOption(Long id, String optionText) {}
}
