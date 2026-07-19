package com.quizsphere.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SubmitAttemptRequest(@NotNull List<@Valid AnswerSubmission> answers) {
    public record AnswerSubmission(@NotNull Long questionId, Long selectedOptionId) {}
}
