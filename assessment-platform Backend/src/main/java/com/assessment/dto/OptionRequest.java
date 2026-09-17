package com.assessment.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for creating or updating an option.
 *
 * The teacher sets isCorrect=true on exactly one option per question.
 * The service layer enforces that only one correct option exists per question.
 */
@Getter
@Setter
@NoArgsConstructor
public class OptionRequest {

    @NotBlank(message = "Option text is required")
    @Size(max = 500, message = "Option text must not exceed 500 characters")
    private String optionText;

    @NotNull(message = "Option order is required")
    @Min(value = 1, message = "Option order must be at least 1")
    private Integer optionOrder;

    @JsonProperty("isCorrect")
    @JsonAlias("correct")
    private boolean isCorrect = false;
}

