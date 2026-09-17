package com.assessment.service;

import com.assessment.dto.OptionRequest;
import com.assessment.dto.OptionResponse;
import com.assessment.entity.Assessment;
import com.assessment.entity.AssessmentStatus;
import com.assessment.entity.Option;
import com.assessment.entity.Question;
import com.assessment.exception.BadRequestException;
import com.assessment.exception.ResourceNotFoundException;
import com.assessment.repository.OptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for managing MCQ options within a question.
 *
 * Rules:
 *  - Options can only be added/edited on DRAFT assessments.
 *  - When marking an option as correct (isCorrect=true), the service
 *    automatically clears the isCorrect flag on all other options for
 *    that question, ensuring exactly one correct answer exists.
 */
@Service
public class OptionService {

    private static final Logger log = LoggerFactory.getLogger(OptionService.class);

    private final OptionRepository optionRepository;
    private final QuestionService questionService;
    private final TeacherAssessmentService teacherAssessmentService;

    public OptionService(OptionRepository optionRepository,
                         QuestionService questionService,
                         TeacherAssessmentService teacherAssessmentService) {
        this.optionRepository = optionRepository;
        this.questionService = questionService;
        this.teacherAssessmentService = teacherAssessmentService;
    }

    @Transactional
    public OptionResponse addOption(Long questionId, OptionRequest request) {
        Question question = questionService.findQuestion(questionId);
        Assessment assessment = question.getAssessment();

        teacherAssessmentService.findAndVerifyOwnership(assessment.getId());

        if (assessment.getStatus() != AssessmentStatus.DRAFT) {
            throw new BadRequestException("Options can only be added to DRAFT assessments");
        }

        // If this option is marked correct, clear any existing correct option
        if (request.isCorrect()) {
            clearCorrectOption(questionId);
        }

        Option option = new Option();
        option.setQuestion(question);
        option.setOptionText(request.getOptionText());
        option.setOptionOrder(request.getOptionOrder());
        option.setCorrect(request.isCorrect());

        Option saved = optionRepository.save(option);
        log.info("Option added: id={}, questionId={}, isCorrect={}", saved.getId(), questionId, saved.isCorrect());
        return new OptionResponse(saved, true); // teacher sees isCorrect
    }

    @Transactional
    public OptionResponse updateOption(Long optionId, OptionRequest request) {
        Option option = findOption(optionId);
        Question question = option.getQuestion();
        Assessment assessment = question.getAssessment();

        teacherAssessmentService.findAndVerifyOwnership(assessment.getId());

        if (assessment.getStatus() != AssessmentStatus.DRAFT) {
            throw new BadRequestException("Options can only be edited on DRAFT assessments");
        }

        // If marking this option as correct, first clear others
        if (request.isCorrect() && !option.isCorrect()) {
            clearCorrectOption(question.getId());
        }

        option.setOptionText(request.getOptionText());
        option.setOptionOrder(request.getOptionOrder());
        option.setCorrect(request.isCorrect());

        return new OptionResponse(optionRepository.save(option), true);
    }

    @Transactional
    public void deleteOption(Long optionId) {
        Option option = findOption(optionId);
        Assessment assessment = option.getQuestion().getAssessment();

        teacherAssessmentService.findAndVerifyOwnership(assessment.getId());

        if (assessment.getStatus() != AssessmentStatus.DRAFT) {
            throw new BadRequestException("Options can only be deleted from DRAFT assessments");
        }

        optionRepository.delete(option);
        log.info("Option deleted: id={}", optionId);
    }

    // ── Helpers ───────────────────────────────────────────────────────

    public Option findOption(Long optionId) {
        return optionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Option not found with id: " + optionId));
    }

    /**
     * Clears the isCorrect flag on all current options for a question.
     * Called before marking a new option as correct to enforce single correct answer.
     */
    private void clearCorrectOption(Long questionId) {
        List<Option> options = optionRepository.findByQuestionIdOrderByOptionOrderAsc(questionId);
        options.forEach(opt -> opt.setCorrect(false));
        optionRepository.saveAll(options);
    }
}
