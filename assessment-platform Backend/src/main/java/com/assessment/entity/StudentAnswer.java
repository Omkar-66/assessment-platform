package com.assessment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Records a student's answer to one question within an attempt.
 *
 * Maps to the `student_answers` table.
 *
 * The DB enforces uniqueness of (attempt_id, question_id), meaning
 * a student can only have one answer per question per attempt.
 *
 * selectedOption is nullable: if a student skips a question,
 * selectedOption will be null and isCorrect will be false.
 *
 * isCorrect and marksObtained are SET BY THE BACKEND on submission.
 * They must never be accepted from the client.
 */
@Entity
@Table(name = "student_answers")
@Getter
@Setter
@NoArgsConstructor
public class StudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The attempt this answer belongs to.
     * Maps to student_answers.attempt_id → assessment_attempts.id.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private AssessmentAttempt attempt;

    /**
     * The question being answered.
     * Maps to student_answers.question_id → questions.id.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /**
     * The option the student chose. NULL means the question was skipped.
     * Maps to student_answers.selected_option_id → options.id.
     * ON DELETE SET NULL is handled at the DB level.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "selected_option_id", nullable = true)
    private Option selectedOption;

    /**
     * Set by the backend during submission scoring. Never from the client.
     */
    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect = false;

    /**
     * Marks awarded for this answer. Set by the backend during scoring.
     * DECIMAL(10,2) in MySQL.
     */
    @Column(name = "marks_obtained", nullable = false, precision = 10, scale = 2)
    private BigDecimal marksObtained = BigDecimal.ZERO;

    @Column(name = "answered_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime answeredAt;
}
