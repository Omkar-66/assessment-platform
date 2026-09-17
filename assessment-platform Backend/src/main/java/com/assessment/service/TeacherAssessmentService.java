package com.assessment.service;

import com.assessment.dto.AssessmentRequest;
import com.assessment.dto.AssessmentResponse;
import com.assessment.dto.AttemptResponse;
import com.assessment.dto.QuestionResponse;
import com.assessment.dto.StudentAnswerResponse;
import com.assessment.entity.Assessment;
import com.assessment.entity.AssessmentStatus;
import com.assessment.entity.User;
import com.assessment.exception.BadRequestException;
import com.assessment.exception.ResourceNotFoundException;
import com.assessment.exception.UnauthorizedException;
import com.assessment.repository.AssessmentAttemptRepository;
import com.assessment.repository.AssessmentRepository;
import com.assessment.repository.OptionRepository;
import com.assessment.repository.QuestionRepository;
import com.assessment.repository.StudentAnswerRepository;
import com.assessment.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Business logic for teacher assessment management.
 *
 * Rules enforced here:
 *  - A teacher can only modify their own assessments.
 *  - Only DRAFT assessments can be edited or deleted.
 *  - An assessment must have at least one question to be published.
 *  - Only PUBLISHED assessments can be closed.
 */
@Service
public class TeacherAssessmentService {

    private static final Logger log = LoggerFactory.getLogger(TeacherAssessmentService.class);

    private final AssessmentRepository assessmentRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final SecurityUtil securityUtil;

    public TeacherAssessmentService(
            AssessmentRepository assessmentRepository,
            QuestionRepository questionRepository,
            OptionRepository optionRepository,
            AssessmentAttemptRepository attemptRepository,
            StudentAnswerRepository studentAnswerRepository,
            SecurityUtil securityUtil) {
        this.assessmentRepository = assessmentRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.attemptRepository = attemptRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.securityUtil = securityUtil;
    }

    // ── Assessment CRUD ──────────────────────────────────────────────────

    @Transactional
    public AssessmentResponse createAssessment(AssessmentRequest request) {
        User teacher = securityUtil.getCurrentUser();

        Assessment assessment = new Assessment();
        assessment.setTitle(request.getTitle());
        assessment.setDescription(request.getDescription());
        assessment.setDurationMinutes(request.getDurationMinutes());
        assessment.setTotalMarks(0);
        assessment.setStatus(AssessmentStatus.DRAFT);
        assessment.setCreatedBy(teacher);
        assessment.setCreatedAt(LocalDateTime.now());
        assessment.setUpdatedAt(LocalDateTime.now());

        Assessment saved = assessmentRepository.save(assessment);
        log.info("Assessment created: id={}, title='{}', teacher={}", saved.getId(), saved.getTitle(), teacher.getUsername());
        return new AssessmentResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> getMyAssessments() {
        User teacher = securityUtil.getCurrentUser();
        return assessmentRepository.findByCreatedById(teacher.getId())
                .stream()
                .map(AssessmentResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssessmentResponse getAssessmentById(Long id) {
        Assessment assessment = findAndVerifyOwnership(id);
        return new AssessmentResponse(assessment);
    }

    @Transactional
    public AssessmentResponse updateAssessment(Long id, AssessmentRequest request) {
        Assessment assessment = findAndVerifyOwnership(id);

        if (assessment.getStatus() != AssessmentStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT assessments can be edited");
        }

        assessment.setTitle(request.getTitle());
        assessment.setDescription(request.getDescription());
        assessment.setDurationMinutes(request.getDurationMinutes());
        assessment.setUpdatedAt(LocalDateTime.now());

        return new AssessmentResponse(assessmentRepository.save(assessment));
    }

    @Transactional
    public void deleteAssessment(Long id) {
        Assessment assessment = findAndVerifyOwnership(id);

        if (assessment.getStatus() != AssessmentStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT assessments can be deleted");
        }

        assessmentRepository.delete(assessment);
        log.info("Assessment deleted: id={}", id);
    }

    // ── Publish / Close ──────────────────────────────────────────────────

    @Transactional
    public AssessmentResponse publishAssessment(Long id) {
        Assessment assessment = findAndVerifyOwnership(id);

        if (assessment.getStatus() != AssessmentStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT assessments can be published");
        }

        long questionCount = questionRepository.countByAssessmentId(id);
        if (questionCount == 0) {
            throw new BadRequestException("Cannot publish an assessment with no questions");
        }

        assessment.setStatus(AssessmentStatus.PUBLISHED);
        assessment.setUpdatedAt(LocalDateTime.now());

        log.info("Assessment published: id={}", id);
        return new AssessmentResponse(assessmentRepository.save(assessment));
    }

    @Transactional
    public AssessmentResponse closeAssessment(Long id) {
        Assessment assessment = findAndVerifyOwnership(id);

        if (assessment.getStatus() != AssessmentStatus.PUBLISHED) {
            throw new BadRequestException("Only PUBLISHED assessments can be closed");
        }

        assessment.setStatus(AssessmentStatus.CLOSED);
        assessment.setUpdatedAt(LocalDateTime.now());

        log.info("Assessment closed: id={}", id);
        return new AssessmentResponse(assessmentRepository.save(assessment));
    }

    // ── Attempt / Result Viewing ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AttemptResponse> getAttemptsForAssessment(Long assessmentId) {
        findAndVerifyOwnership(assessmentId);
        return attemptRepository.findByAssessmentId(assessmentId)
                .stream()
                .map(AttemptResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public AttemptResponse getAttemptDetail(Long attemptId) {
        var attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found with id: " + attemptId));

        // Verify the attempt belongs to an assessment owned by this teacher
        findAndVerifyOwnership(attempt.getAssessment().getId());
        return new AttemptResponse(attempt);
    }

    @Transactional(readOnly = true)
    public List<StudentAnswerResponse> getAnswersForAttempt(Long attemptId) {
        var attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found with id: " + attemptId));

        findAndVerifyOwnership(attempt.getAssessment().getId());

        return studentAnswerRepository.findByAttemptId(attemptId)
                .stream()
                .map(answer -> {
                    com.assessment.entity.Option correctOpt = optionRepository
                            .findByQuestionIdAndIsCorrectTrue(answer.getQuestion().getId())
                            .orElse(null);
                    Long correctOptId = correctOpt != null ? correctOpt.getId() : null;
                    String correctOptText = correctOpt != null ? correctOpt.getOptionText() : null;
                    return new StudentAnswerResponse(answer, correctOptId, correctOptText);
                })
                .toList();
    }

    // ── Helper ───────────────────────────────────────────────────────────

    /**
     * Loads an assessment and verifies the current teacher owns it.
     * Throws ResourceNotFoundException or UnauthorizedException as appropriate.
     */
    public Assessment findAndVerifyOwnership(Long assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assessment not found with id: " + assessmentId));

        User currentTeacher = securityUtil.getCurrentUser();
        if (!assessment.getCreatedBy().getId().equals(currentTeacher.getId())) {
            throw new UnauthorizedException("You do not have permission to access this assessment");
        }

        return assessment;
    }

    /**
     * Recalculates and saves the totalMarks on the assessment
     * by summing the marks of all its questions.
     * Called after any question is added, updated, or deleted.
     */
    @Transactional
    public void recalculateTotalMarks(Assessment assessment) {
        int total = questionRepository.findByAssessmentId(assessment.getId())
                .stream()
                .mapToInt(q -> q.getMarks())
                .sum();

        assessment.setTotalMarks(total);
        assessment.setUpdatedAt(LocalDateTime.now());
        assessmentRepository.save(assessment);
    }
}
