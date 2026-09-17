package com.assessment.dto;

import com.assessment.entity.Option;
import lombok.Getter;

/**
 * Response body representing one answer option.
 *
 * The isCorrect field is conditionally included:
 *  - Teacher view: isCorrect is shown so teachers can see which is correct.
 *  - Student view: isCorrect is always null/hidden to prevent cheating.
 *
 * The showCorrectAnswer boolean is passed in from QuestionResponse
 * based on the calling context (teacher vs student).
 */
@Getter
public class OptionResponse {

    private final Long id;
    private final Long questionId;
    private final String optionText;
    private final Integer optionOrder;

    /**
     * Null when showCorrectAnswer is false (student view).
     * True/false when showCorrectAnswer is true (teacher view).
     */
    private final Boolean isCorrect;

    public OptionResponse(Option option, boolean showCorrectAnswer) {
        this.id = option.getId();
        this.questionId = option.getQuestion().getId();
        this.optionText = option.getOptionText();
        this.optionOrder = option.getOptionOrder();
        this.isCorrect = showCorrectAnswer ? option.isCorrect() : null;
    }
}
