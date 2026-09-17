package com.assessment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for saving or updating a student's answer to one question.
 *
 * selectedOptionId is nullable — a null value means the student
 * explicitly skipped this question (no answer selected).
 *
 * For POST /api/student/attempts/{id}/answers:
 *   Both questionId and selectedOptionId must be provided.
 *
 * For PUT /api/student/attempts/{id}/answers/{questionId}:
 *   Only selectedOptionId is needed (questionId comes from the path variable).
 */
@Getter
@Setter
@NoArgsConstructor
public class AnswerRequest {

    private Long questionId;

    // Nullable: null = student chose to skip this question
    private Long selectedOptionId;
}
