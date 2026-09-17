package com.assessment.repository;

import com.assessment.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access layer for the Question entity.
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /** Retrieves all questions for an assessment, ordered for display. */
    List<Question> findByAssessmentIdOrderByQuestionOrderAsc(Long assessmentId);

    /** Used to recalculate total marks when a question is added or removed. */
    List<Question> findByAssessmentId(Long assessmentId);

    /** Counts how many questions are in an assessment. */
    long countByAssessmentId(Long assessmentId);
}
