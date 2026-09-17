package com.assessment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Records one student's attempt at a specific assessment.
 *
 * Maps to the `assessment_attempts` table.
 *
 * Flow:
 *   1. Created with status IN_PROGRESS when a student starts an assessment.
 *   2. Student submits answers via StudentAnswer.
 *   3. On submission, score/totalMarks/percentage are calculated by the
 *      backend and status changes to SUBMITTED.
 *
 * Score fields use BigDecimal to match DECIMAL(10,2) / DECIMAL(5,2) in MySQL.
 */
@Entity
@Table(name = "assessment_attempts")
@Getter
@Setter
@NoArgsConstructor
public class AssessmentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The assessment being attempted.
     * Maps to assessment_attempts.assessment_id → assessments.id.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    /**
     * The student who is attempting the assessment.
     * Maps to assessment_attempts.student_id → users.id.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    /**
     * Marks earned. Calculated by the backend on submission.
     * DECIMAL(10,2) in MySQL.
     */
    @Column(name = "score", precision = 10, scale = 2)
    private BigDecimal score = BigDecimal.ZERO;

    /**
     * Total possible marks for this attempt (snapshot of assessment.totalMarks
     * at submission time). DECIMAL(10,2) in MySQL.
     */
    @Column(name = "total_marks", precision = 10, scale = 2)
    private BigDecimal totalMarks = BigDecimal.ZERO;

    /**
     * (score / totalMarks) * 100. DECIMAL(5,2) in MySQL.
     */
    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(name = "started_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime startedAt;

    /**
     * Null until the attempt is submitted.
     */
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    /**
     * Calculates the expiration time based on startedAt and assessment.durationMinutes.
     */
    public LocalDateTime getExpirationTime() {
        if (startedAt == null || assessment == null || assessment.getDurationMinutes() == null) {
            return null;
        }
        return startedAt.plusMinutes(assessment.getDurationMinutes());
    }

    /**
     * Checks if this attempt has exceeded its allowed duration.
     */
    public boolean isExpired() {
        LocalDateTime expirationTime = getExpirationTime();
        return expirationTime != null && LocalDateTime.now().isAfter(expirationTime);
    }
}

