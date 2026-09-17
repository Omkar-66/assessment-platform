package com.assessment.dto;

import com.assessment.entity.AssessmentAttempt;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response body for an assessment attempt.
 * Used in both teacher (viewing all student attempts) and student (viewing own result) contexts.
 */
@Getter
public class AttemptResponse {

    private final Long id;
    private final Long assessmentId;
    private final String assessmentTitle;
    private final Long studentId;
    private final String studentUsername;
    private final BigDecimal score;
    private final BigDecimal totalMarks;
    private final BigDecimal percentage;
    private final String status;
    private final LocalDateTime startedAt;
    private final LocalDateTime submittedAt;

    public AttemptResponse(AssessmentAttempt attempt) {
        this.id = attempt.getId();
        this.assessmentId = attempt.getAssessment().getId();
        this.assessmentTitle = attempt.getAssessment().getTitle();
        this.studentId = attempt.getStudent().getId();
        this.studentUsername = attempt.getStudent().getUsername();
        this.score = attempt.getScore();
        this.totalMarks = attempt.getTotalMarks();
        this.percentage = attempt.getPercentage();
        this.status = attempt.getStatus().name();
        this.startedAt = attempt.getStartedAt();
        this.submittedAt = attempt.getSubmittedAt();
    }
}

