package com.assessment.dto;

import com.assessment.entity.AssessmentAttempt;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response for a student viewing an in-progress attempt.
 *
 * Returned when:
 *  - A student starts an assessment (POST /api/student/assessments/{id}/start)
 *  - A student views an ongoing attempt (GET /api/student/attempts/{id})
 *
 * Questions and options are included so the student can answer them.
 * IMPORTANT: isCorrect is always null in these options (showCorrectAnswer=false).
 */
@Getter
public class AttemptWithQuestionsResponse {

    private final Long attemptId;
    private final String status;
    private final LocalDateTime startedAt;

    private final Long assessmentId;
    private final String assessmentTitle;
    private final String assessmentDescription;
    private final Integer durationMinutes;
    private final Integer totalMarks;

    private final List<QuestionResponse> questions;

    public AttemptWithQuestionsResponse(AssessmentAttempt attempt, List<QuestionResponse> questions) {
        this.attemptId = attempt.getId();
        this.status = attempt.getStatus().name();
        this.startedAt = attempt.getStartedAt();

        this.assessmentId = attempt.getAssessment().getId();
        this.assessmentTitle = attempt.getAssessment().getTitle();
        this.assessmentDescription = attempt.getAssessment().getDescription();
        this.durationMinutes = attempt.getAssessment().getDurationMinutes();
        this.totalMarks = attempt.getAssessment().getTotalMarks();

        this.questions = questions;
    }
}
