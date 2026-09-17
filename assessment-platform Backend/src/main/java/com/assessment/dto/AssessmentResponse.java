package com.assessment.dto;

import com.assessment.entity.Assessment;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Response body representing an assessment.
 *
 * Used in both teacher and student contexts.
 * For student responses, the list of questions/options is handled
 * separately to ensure correct answers are never exposed.
 */
@Getter
public class AssessmentResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final Integer durationMinutes;
    private final Integer totalMarks;
    private final String status;
    private final Long createdById;
    private final String createdByUsername;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public AssessmentResponse(Assessment assessment) {
        this.id = assessment.getId();
        this.title = assessment.getTitle();
        this.description = assessment.getDescription();
        this.durationMinutes = assessment.getDurationMinutes();
        this.totalMarks = assessment.getTotalMarks();
        this.status = assessment.getStatus().name();
        this.createdById = assessment.getCreatedBy().getId();
        this.createdByUsername = assessment.getCreatedBy().getUsername();
        this.createdAt = assessment.getCreatedAt();
        this.updatedAt = assessment.getUpdatedAt();
    }
}
