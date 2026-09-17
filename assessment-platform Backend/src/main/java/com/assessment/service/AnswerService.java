package com.assessment.service;

import com.assessment.dto.AnswerRequest;
import com.assessment.dto.StudentAnswerResponse;
import com.assessment.entity.AssessmentAttempt;
import com.assessment.entity.AttemptStatus;
import com.assessment.entity.Option;
import com.assessment.entity.Question;
import com.assessment.entity.StudentAnswer;
import com.assessment.exception.AssessmentAlreadySubmittedException;
import com.assessment.exception.BadRequestException;
import com.assessment.exception.ResourceNotFoundException;
import com.assessment.repository.OptionRepository;
import com.assessment.repository.QuestionRepository;
import com.assessment.repository.StudentAnswerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handles saving and updating student answers during an in-progress attempt.
 */
@Service
public class AnswerService {

    private static final Logger log = LoggerFactory.getLogger(AnswerService.class);

    private final StudentAnswerRepository studentAnswerRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final StudentService studentService;
    private final ScoringService scoringService;

    public AnswerService(StudentAnswerRepository studentAnswerRepository,
                         QuestionRepository questionRepository,
                         OptionRepository optionRepository,
                         StudentService studentService,
                         @Lazy ScoringService scoringService) {
        this.studentAnswerRepository = studentAnswerRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.studentService = studentService;
        this.scoringService = scoringService;
    }

    @Transactional
    public StudentAnswerResponse saveOrUpdateAnswer(
            Long attemptId, Long questionId, Long selectedOptionId) {

        AssessmentAttempt attempt = studentService.findAttemptForCurrentStudent(attemptId);

        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            throw new AssessmentAlreadySubmittedException(
                    "This attempt has already been submitted and cannot be modified");
        }

        // Server-Side Duration Expiration Enforcement:
        if (attempt.isExpired()) {
            log.info("Attempt {} has expired. Auto-submitting attempt.", attemptId);
            scoringService.submitAttempt(attemptId);
            throw new BadRequestException("Assessment time limit has expired. Your attempt has been automatically submitted.");
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Question not found with id: " + questionId));

        // Verify the question belongs to the attempt's assessment
        if (!question.getAssessment().getId().equals(attempt.getAssessment().getId())) {
            throw new BadRequestException(
                    "Question " + questionId + " does not belong to this assessment");
        }

        // Validate the selected option belongs to the question (if provided)
        Option selectedOption = null;
        if (selectedOptionId != null) {
            selectedOption = optionRepository.findById(selectedOptionId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Option not found with id: " + selectedOptionId));

            if (!selectedOption.getQuestion().getId().equals(questionId)) {
                throw new BadRequestException(
                        "Option " + selectedOptionId + " does not belong to question " + questionId);
            }
        }

        // Upsert: update existing answer or create new one
        StudentAnswer answer = studentAnswerRepository
                .findByAttemptIdAndQuestionId(attemptId, questionId)
                .orElse(new StudentAnswer());

        answer.setAttempt(attempt);
        answer.setQuestion(question);
        answer.setSelectedOption(selectedOption);
        answer.setCorrect(false);       // scoring happens at submission, not here
        answer.setMarksObtained(java.math.BigDecimal.ZERO);
        answer.setAnsweredAt(LocalDateTime.now());

        StudentAnswer saved = studentAnswerRepository.save(answer);
        log.debug("Answer saved: attemptId={}, questionId={}, optionId={}",
                attemptId, questionId, selectedOptionId);

        return new StudentAnswerResponse(saved);
    }
}
