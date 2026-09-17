package com.assessment.repository;

import com.assessment.entity.Assessment;
import com.assessment.entity.AssessmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access layer for the Assessment entity.
 */
@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    /** Used by teachers to list their own assessments. */
    List<Assessment> findByCreatedById(Long teacherId);

    /** Used by students to list available assessments. */
    List<Assessment> findByStatus(AssessmentStatus status);
}
