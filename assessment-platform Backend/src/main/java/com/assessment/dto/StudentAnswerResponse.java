package com.assessment.dto;

import com.assessment.entity.StudentAnswer;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response body representing one student answer within an attempt.
 *
 * Includes correctOption details for teacher attempt review and post-submission results.
 */
@Getter
public class StudentAnswerResponse {

    private final Long id;
    private final Long questionId;
    private final String questionText;
    private final Long selectedOptionId;
    private final String selectedOptionText;
    private final Long correctOptionId;
    private final String correctOptionText;

    @JsonProperty("isCorrect")
    private final boolean correct;

    private final BigDecimal marksObtained;
    private final LocalDateTime answeredAt;

    public StudentAnswerResponse(StudentAnswer answer) {
        this(answer, null, null);
    }

    public StudentAnswerResponse(StudentAnswer answer, Long correctOptionId, String correctOptionText) {
        this.id = answer.getId();
        this.questionId = answer.getQuestion().getId();
        this.questionText = answer.getQuestion().getQuestionText();
        this.selectedOptionId = answer.getSelectedOption() != null
                ? answer.getSelectedOption().getId() : null;
        this.selectedOptionText = answer.getSelectedOption() != null
                ? answer.getSelectedOption().getOptionText() : null;
        this.correctOptionId = correctOptionId;
        this.correctOptionText = correctOptionText;
        this.correct = answer.isCorrect();
        this.marksObtained = answer.getMarksObtained();
        this.answeredAt = answer.getAnsweredAt();
    }
}

