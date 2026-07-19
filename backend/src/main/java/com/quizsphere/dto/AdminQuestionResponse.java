package com.quizsphere.dto;

import java.util.List;

public record AdminQuestionResponse(
        Long id,
        String questionText,
        String explanation,
        Integer marks,
        List<AdminOptionResponse> options
) {
    public record AdminOptionResponse(Long id, String optionText, boolean correct) {}
}
