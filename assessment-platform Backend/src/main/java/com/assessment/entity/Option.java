package com.assessment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents one answer choice for a question.
 *
 * Maps to the `options` table.
 *
 * isCorrect identifies the correct answer. This field MUST NEVER be
 * exposed in student-facing API responses – the service layer is
 * responsible for hiding it via the DTO mapping.
 *
 * The DB enforces uniqueness of (question_id, option_order).
 */
@Entity
@Table(name = "options")
@Getter
@Setter
@NoArgsConstructor
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The question this option belongs to.
     * Maps to options.question_id → questions.id.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "option_text", nullable = false, length = 500)
    private String optionText;

    @Column(name = "option_order", nullable = false)
    private Integer optionOrder;

    /**
     * TRUE for the correct answer option. Only one option per question
     * should have this set to true (enforced at the service layer).
     *
     * tinyint(1) in MySQL maps naturally to boolean in Java/Hibernate.
     */
    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect = false;
}
