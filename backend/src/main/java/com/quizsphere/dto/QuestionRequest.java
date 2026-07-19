package com.quizsphere.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record QuestionRequest(
        @NotBlank String questionText,
        String explanation,
        @NotNull @Min(1) @Max(100) Integer marks,
        @NotNull @Size(min = 2, max = 6) List<@Valid OptionRequest> options
) {}
