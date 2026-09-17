package com.assessment.repository;

import com.assessment.entity.AssessmentAttempt;
import com.assessment.entity.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access layer for the AssessmentAttempt entity.
 */
@Repository
public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, Long> {

    /** Used by a student to list all their own attempts. */
    List<AssessmentAttempt> findByStudentId(Long studentId);

    /** Used by a teacher to see all attempts for a specific assessment. */
    List<AssessmentAttempt> findByAssessmentId(Long assessmentId);

    /**
     * Checks if a student already has an in-progress or submitted attempt
     * for a given assessment. Prevents duplicate attempts.
     */
    Optional<AssessmentAttempt> findByAssessmentIdAndStudentIdAndStatus(
            Long assessmentId, Long studentId, AttemptStatus status);

    /**
     * Checks whether a student has any existing attempt (any status)
     * for a given assessment. Used to prevent re-attempts.
     */
    boolean existsByAssessmentIdAndStudentId(Long assessmentId, Long studentId);
}
