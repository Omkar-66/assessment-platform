package com.assessment.dto;

import com.assessment.entity.AssessmentAttempt;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response for a student viewing their completed attempt result.
 *
 * Returned by GET /api/student/attempts/{id}/result.
 *
 * Includes the overall score and a per-question breakdown showing
 * what the student selected and whether it was correct.
 *
 * NOTE: This response is only available after the attempt is SUBMITTED.
 * The correct answer identity is implicitly revealed here via the
 * isCorrect flag on each StudentAnswerResponse — this is intentional
 * for post-submission review, not pre-submission.
 */
@Getter
public class AttemptResultResponse {

    private final Long attemptId;
    private final Long assessmentId;
    private final String assessmentTitle;
    private final BigDecimal score;
    private final BigDecimal totalMarks;
    private final BigDecimal percentage;
    private final String status;
    private final LocalDateTime startedAt;
    private final LocalDateTime submittedAt;

    private final List<StudentAnswerResponse> answers;

    public AttemptResultResponse(AssessmentAttempt attempt, List<StudentAnswerResponse> answers) {
        this.attemptId = attempt.getId();
        this.assessmentId = attempt.getAssessment().getId();
        this.assessmentTitle = attempt.getAssessment().getTitle();
        this.score = attempt.getScore();
        this.totalMarks = attempt.getTotalMarks();
        this.percentage = attempt.getPercentage();
        this.status = attempt.getStatus().name();
        this.startedAt = attempt.getStartedAt();
        this.submittedAt = attempt.getSubmittedAt();
        this.answers = answers;
    }
}
