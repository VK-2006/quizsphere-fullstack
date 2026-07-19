package com.quizsphere.dto;

import java.util.List;

public record ReviewResponse(Long attemptId, String quizTitle, List<ReviewItem> items) {
    public record ReviewItem(
            Long questionId,
            String questionText,
            String selectedAnswer,
            String correctAnswer,
            boolean correct,
            Integer marksAwarded,
            Integer totalMarks,
            String explanation
    ) {}
}
