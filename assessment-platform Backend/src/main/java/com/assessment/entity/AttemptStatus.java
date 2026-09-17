package com.assessment.entity;

/**
 * Represents the status of a student's attempt at an assessment.
 * Maps to the ENUM('IN_PROGRESS','SUBMITTED') column in the assessment_attempts table.
 *
 * IN_PROGRESS – The student has started but not yet submitted.
 * SUBMITTED   – The attempt has been submitted and scored.
 */
public enum AttemptStatus {
    IN_PROGRESS,
    SUBMITTED
}
