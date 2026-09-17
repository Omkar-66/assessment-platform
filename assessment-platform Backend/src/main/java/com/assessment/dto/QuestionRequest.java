package com.assessment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for creating or updating a question.
 */
@Getter
@Setter
@NoArgsConstructor
public class QuestionRequest {

    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotNull(message = "Marks are required")
    @Min(value = 1, message = "Marks must be at least 1")
    private Integer marks;

    @NotNull(message = "Question order is required")
    @Min(value = 1, message = "Question order must be at least 1")
    private Integer questionOrder;
}
