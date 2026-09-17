package com.assessment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a single MCQ question belonging to an assessment.
 *
 * Maps to the `questions` table.
 *
 * question_order controls the display sequence within an assessment.
 * The DB enforces uniqueness of (assessment_id, question_order).
 *
 * Options are fetched via OptionRepository; they are NOT held as a
 * collection here to keep this entity focused.
 */
@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The assessment this question belongs to.
     * Maps to questions.assessment_id → assessments.id.
     * CASCADE on delete is handled at the DB level (ON DELETE CASCADE).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "marks", nullable = false)
    private Integer marks = 1;

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;
}
