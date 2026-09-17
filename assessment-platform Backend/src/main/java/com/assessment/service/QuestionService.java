package com.assessment.service;

import com.assessment.dto.QuestionRequest;
import com.assessment.dto.QuestionResponse;
import com.assessment.entity.Assessment;
import com.assessment.entity.Option;
import com.assessment.entity.Question;
import com.assessment.exception.BadRequestException;
import com.assessment.exception.ResourceNotFoundException;
import com.assessment.entity.AssessmentStatus;
import com.assessment.repository.OptionRepository;
import com.assessment.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for managing questions within an assessment.
 *
 * Rules:
 *  - Questions can only be added/edited on DRAFT assessments.
 *  - When a question is added, updated, or deleted, the assessment's
 *    totalMarks is recalculated automatically.
 */
@Service
public class QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);

    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final TeacherAssessmentService teacherAssessmentService;

    public QuestionService(QuestionRepository questionRepository,
                           OptionRepository optionRepository,
                           TeacherAssessmentService teacherAssessmentService) {
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.teacherAssessmentService = teacherAssessmentService;
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsForAssessment(Long assessmentId) {
        teacherAssessmentService.findAndVerifyOwnership(assessmentId);
        return questionRepository.findByAssessmentIdOrderByQuestionOrderAsc(assessmentId)
                .stream()
                .map(this::buildQuestionResponse)
                .toList();
    }

    @Transactional
    public QuestionResponse addQuestion(Long assessmentId, QuestionRequest request) {
        Assessment assessment = teacherAssessmentService.findAndVerifyOwnership(assessmentId);

        if (assessment.getStatus() != AssessmentStatus.DRAFT) {
            throw new BadRequestException("Questions can only be added to DRAFT assessments");
        }

        Question question = new Question();
        question.setAssessment(assessment);
        question.setQuestionText(request.getQuestionText());
        question.setMarks(request.getMarks());
        question.setQuestionOrder(request.getQuestionOrder());

        Question saved = questionRepository.save(question);
        teacherAssessmentService.recalculateTotalMarks(assessment);

        log.info("Question added: id={}, assessmentId={}", saved.getId(), assessmentId);
        return buildQuestionResponse(saved);
    }

    @Transactional
    public QuestionResponse updateQuestion(Long questionId, QuestionRequest request) {
        Question question = findQuestion(questionId);
        Assessment assessment = question.getAssessment();

        teacherAssessmentService.findAndVerifyOwnership(assessment.getId());

        if (assessment.getStatus() != AssessmentStatus.DRAFT) {
            throw new BadRequestException("Questions can only be edited on DRAFT assessments");
        }

        question.setQuestionText(request.getQuestionText());
        question.setMarks(request.getMarks());
        question.setQuestionOrder(request.getQuestionOrder());

        Question saved = questionRepository.save(question);
        teacherAssessmentService.recalculateTotalMarks(assessment);

        return buildQuestionResponse(saved);
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        Question question = findQuestion(questionId);
        Assessment assessment = question.getAssessment();

        teacherAssessmentService.findAndVerifyOwnership(assessment.getId());

        if (assessment.getStatus() != AssessmentStatus.DRAFT) {
            throw new BadRequestException("Questions can only be deleted from DRAFT assessments");
        }

        questionRepository.delete(question);
        teacherAssessmentService.recalculateTotalMarks(assessment);

        log.info("Question deleted: id={}", questionId);
    }

    // ── Helpers ───────────────────────────────────────────────────────

    public Question findQuestion(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Question not found with id: " + questionId));
    }

    @Transactional(readOnly = true)
    private QuestionResponse buildQuestionResponse(Question question) {
        List<Option> options = optionRepository
                .findByQuestionIdOrderByOptionOrderAsc(question.getId());
        return new QuestionResponse(question, options, true); // teacher sees isCorrect
    }
}
