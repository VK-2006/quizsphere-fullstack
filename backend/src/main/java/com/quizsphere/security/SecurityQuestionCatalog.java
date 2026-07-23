package com.quizsphere.security;

import com.quizsphere.exception.BadRequestException;

import java.util.List;

public final class SecurityQuestionCatalog {
    public static final List<String> QUESTIONS = List.of(
            "What is your favorite color?",
            "What is your favorite place?",
            "What is your favorite food?",
            "What is a memorable nickname known only to you?",
            "What private recovery word did you choose?"
    );

    private SecurityQuestionCatalog() {}

    public static String validate(String question) {
        String normalized = question == null ? "" : question.trim();
        if (!QUESTIONS.contains(normalized)) {
            throw new BadRequestException("Choose a valid security question");
        }
        return normalized;
    }
}
