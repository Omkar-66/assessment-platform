package com.assessment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents an assessment created by a teacher.
 *
 * Maps to the `assessments` table.
 *
 * Lifecycle:
 *   DRAFT → teacher adds questions and options
 *   PUBLISHED → students can view and attempt it
 *   CLOSED → no new attempts allowed
 *
 * Questions are fetched via QuestionRepository; they are NOT held as a
 * collection here to keep this entity lightweight.
 */
@Entity
@Table(name = "assessments")
@Getter
@Setter
@NoArgsConstructor
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    /**
     * Maintained by the database; updated whenever questions are
     * added or removed (via a service-level recalculation).
     */
    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssessmentStatus status = AssessmentStatus.DRAFT;

    /**
     * The teacher who created this assessment.
     * Maps to assessments.created_by → users.id.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}
