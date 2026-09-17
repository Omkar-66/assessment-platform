package com.assessment.repository;

import com.assessment.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access layer for the Option entity.
 */
@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {

    /** Retrieves all options for a question, ordered for display. */
    List<Option> findByQuestionIdOrderByOptionOrderAsc(Long questionId);

    /** Used during scoring to find the correct answer for a question. */
    Optional<Option> findByQuestionIdAndIsCorrectTrue(Long questionId);

    /** Counts how many options a question has. */
    long countByQuestionId(Long questionId);
}
