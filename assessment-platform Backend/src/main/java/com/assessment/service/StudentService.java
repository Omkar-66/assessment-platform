package com.assessment.service;

import com.assessment.dto.AssessmentResponse;
import com.assessment.dto.AttemptResponse;
import com.assessment.dto.AttemptResultResponse;
import com.assessment.dto.AttemptWithQuestionsResponse;
import com.assessment.dto.QuestionResponse;
import com.assessment.dto.StudentAnswerResponse;
import com.assessment.entity.Assessment;
import com.assessment.entity.AssessmentAttempt;
import com.assessment.entity.AssessmentStatus;
import com.assessment.entity.AttemptStatus;
import com.assessment.entity.Option;
import com.assessment.entity.Question;
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
 * Handles the student-facing assessment flow:
 *  - Browsing published assessments
 *  - Starting an attempt
 *  - Viewing an in-progress attempt with questions
 *  - Viewing their own attempt history
 *  - Viewing their result after submission
 *
 * SECURITY: isCorrect is NEVER passed as true to any QuestionResponse built
 * in this service. All student-facing questions use showCorrectAnswer=false.
 */
@Service
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    private final AssessmentRepository assessmentRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final SecurityUtil securityUtil;

    public StudentService(AssessmentRepository assessmentRepository,
                          AssessmentAttemptRepository attemptRepository,
                          QuestionRepository questionRepository,
                          OptionRepository optionRepository,
                          StudentAnswerRepository studentAnswerRepository,
                          SecurityUtil securityUtil) {
        this.assessmentRepository = assessmentRepository;
        this.attemptRepository = attemptRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.securityUtil = securityUtil;
    }

    // ── Browse ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AssessmentResponse> getPublishedAssessments() {
        return assessmentRepository.findByStatus(AssessmentStatus.PUBLISHED)
                .stream()
                .map(AssessmentResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssessmentResponse getPublishedAssessmentById(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assessment not found with id: " + id));

        if (assessment.getStatus() != AssessmentStatus.PUBLISHED) {
            throw new BadRequestException("This assessment is not currently available");
        }

        return new AssessmentResponse(assessment);
    }

    // ── Start Attempt ────────────────────────────────────────────────────

    /**
     * Creates a new IN_PROGRESS attempt for the current student.
     *
     * Validations:
     *  - Assessment must be PUBLISHED
     *  - Student must not already have an attempt for this assessment
     *
     * Returns the attempt along with all questions and options
     * (isCorrect is hidden — showCorrectAnswer = false).
     */
    @Transactional
    public AttemptWithQuestionsResponse startAttempt(Long assessmentId) {
        User student = securityUtil.getCurrentUser();

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assessment not found with id: " + assessmentId));

        if (assessment.getStatus() != AssessmentStatus.PUBLISHED) {
            throw new BadRequestException("Only PUBLISHED assessments can be attempted");
        }

        if (attemptRepository.existsByAssessmentIdAndStudentId(assessmentId, student.getId())) {
            throw new BadRequestException(
                    "You have already attempted this assessment. Each assessment can only be attempted once.");
        }

        AssessmentAttempt attempt = new AssessmentAttempt();
        attempt.setAssessment(assessment);
        attempt.setStudent(student);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setStartedAt(LocalDateTime.now());

        AssessmentAttempt saved = attemptRepository.save(attempt);
        log.info("Attempt started: id={}, student={}, assessment={}",
                saved.getId(), student.getUsername(), assessment.getTitle());

        return buildAttemptWithQuestions(saved);
    }

    // ── View In-Progress Attempt ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public AttemptWithQuestionsResponse getAttemptWithQuestions(Long attemptId) {
        AssessmentAttempt attempt = findAttemptForCurrentStudent(attemptId);
        return buildAttemptWithQuestions(attempt);
    }

    // ── View Own Attempts ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AttemptResponse> getMyAttempts() {
        User student = securityUtil.getCurrentUser();
        return attemptRepository.findByStudentId(student.getId())
                .stream()
                .map(AttemptResponse::new)
                .toList();
    }

    // ── View Result (after submission) ───────────────────────────────────

    @Transactional(readOnly = true)
    public AttemptResultResponse getAttemptResult(Long attemptId) {
        AssessmentAttempt attempt = findAttemptForCurrentStudent(attemptId);

        if (attempt.getStatus() != AttemptStatus.SUBMITTED) {
            throw new BadRequestException(
                    "Results are only available after the attempt has been submitted");
        }

        List<StudentAnswerResponse> answers = studentAnswerRepository
                .findByAttemptId(attemptId)
                .stream()
                .map(answer -> {
                    Option correctOpt = optionRepository
                            .findByQuestionIdAndIsCorrectTrue(answer.getQuestion().getId())
                            .orElse(null);
                    Long correctOptId = correctOpt != null ? correctOpt.getId() : null;
                    String correctOptText = correctOpt != null ? correctOpt.getOptionText() : null;
                    return new StudentAnswerResponse(answer, correctOptId, correctOptText);
                })
                .toList();

        return new AttemptResultResponse(attempt, answers);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    /**
     * Loads an attempt and verifies it belongs to the current student.
     */
    public AssessmentAttempt findAttemptForCurrentStudent(Long attemptId) {
        AssessmentAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attempt not found with id: " + attemptId));

        User student = securityUtil.getCurrentUser();
        if (!attempt.getStudent().getId().equals(student.getId())) {
            throw new UnauthorizedException("You do not have access to this attempt");
        }

        return attempt;
    }

    /**
     * Builds an AttemptWithQuestionsResponse.
     * All questions use showCorrectAnswer=false — correct answers are never revealed.
     */
    private AttemptWithQuestionsResponse buildAttemptWithQuestions(AssessmentAttempt attempt) {
        List<Question> questions = questionRepository
                .findByAssessmentIdOrderByQuestionOrderAsc(attempt.getAssessment().getId());

        List<QuestionResponse> questionResponses = questions.stream()
                .map(q -> {
                    List<Option> options = optionRepository
                            .findByQuestionIdOrderByOptionOrderAsc(q.getId());
                    return new QuestionResponse(q, options, false); // false = hide isCorrect
                })
                .toList();

        return new AttemptWithQuestionsResponse(attempt, questionResponses);
    }
}
