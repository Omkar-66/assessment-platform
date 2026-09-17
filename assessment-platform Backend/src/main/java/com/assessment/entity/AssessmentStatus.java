package com.assessment.entity;

/**
 * Represents the lifecycle status of an assessment.
 * Maps to the ENUM('DRAFT','PUBLISHED','CLOSED') column in the assessments table.
 *
 * DRAFT     – Teacher is still building the assessment; students cannot see it.
 * PUBLISHED – Students can view and attempt this assessment.
 * CLOSED    – No new attempts are allowed.
 */
public enum AssessmentStatus {
    DRAFT,
    PUBLISHED,
    CLOSED
}
