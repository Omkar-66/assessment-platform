package com.assessment.dto;

import com.assessment.entity.Option;
import com.assessment.entity.Question;
import lombok.Getter;

import java.util.List;

/**
 * Response body representing a question.
 *
 * Includes the list of options. The showCorrectAnswer flag controls
 * whether the isCorrect field is visible:
 *  - true  → teacher view (isCorrect is shown)
 *  - false → student view (isCorrect is hidden)
 */
@Getter
public class QuestionResponse {

    private final Long id;
    private final Long assessmentId;
    private final String questionText;
    private final Integer marks;
    private final Integer questionOrder;
    private final List<OptionResponse> options;

    /**
     * Builds a QuestionResponse. Pass showCorrectAnswer=true for teacher APIs,
     * false for student APIs.
     */
    public QuestionResponse(Question question, List<Option> options, boolean showCorrectAnswer) {
        this.id = question.getId();
        this.assessmentId = question.getAssessment().getId();
        this.questionText = question.getQuestionText();
        this.marks = question.getMarks();
        this.questionOrder = question.getQuestionOrder();
        this.options = options.stream()
                .map(opt -> new OptionResponse(opt, showCorrectAnswer))
                .toList();
    }
}
