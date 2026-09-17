package com.assessment.repository;

import com.assessment.entity.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access layer for the StudentAnswer entity.
 */
@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {

    /** Retrieves all answers for a given attempt (used in result display). */
    List<StudentAnswer> findByAttemptId(Long attemptId);

    /**
     * Finds the answer a student gave for a specific question in a specific attempt.
     * Used when updating an existing answer before submission.
     */
    Optional<StudentAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);

    /**
     * Checks if an answer already exists for a question in an attempt.
     * Determines whether to INSERT or UPDATE.
     */
    boolean existsByAttemptIdAndQuestionId(Long attemptId, Long questionId);
}
