package com.assessment.service;

import com.assessment.dto.AttemptResponse;
import com.assessment.entity.AssessmentAttempt;
import com.assessment.entity.AttemptStatus;
import com.assessment.entity.Option;
import com.assessment.entity.Question;
import com.assessment.entity.StudentAnswer;
import com.assessment.exception.AssessmentAlreadySubmittedException;
import com.assessment.repository.AssessmentAttemptRepository;
import com.assessment.repository.OptionRepository;
import com.assessment.repository.QuestionRepository;
import com.assessment.repository.StudentAnswerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handles assessment submission and server-side scoring.
 *
 * SECURITY RULE: The frontend never sends a score. The backend
 * calculates everything by comparing the student's selected options
 * against the correct options stored in the database.
 *
 * Scoring algorithm (per question):
 *   1. Find the student's answer for this question (may not exist if skipped).
 *   2. Find the correct option for this question from the database.
 *   3. If student's selectedOption == correct option → isCorrect=true, marks=question.marks
 *   4. Otherwise → isCorrect=false, marks=0
 *   5. Sum all marks → score
 *   6. percentage = (score / totalMarks) * 100, rounded to 2 decimal places
 *
 * Unanswered questions:
 *   A StudentAnswer record is created with selectedOption=null,
 *   isCorrect=false, marksObtained=0.
 *   This ensures every question in the assessment has an answer record.
 */
@Service
public class ScoringService {

    private static final Logger log = LoggerFactory.getLogger(ScoringService.class);

    private final AssessmentAttemptRepository attemptRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final StudentService studentService;

    public ScoringService(AssessmentAttemptRepository attemptRepository,
                          QuestionRepository questionRepository,
                          OptionRepository optionRepository,
                          StudentAnswerRepository studentAnswerRepository,
                          StudentService studentService) {
        this.attemptRepository = attemptRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.studentService = studentService;
    }

    /**
     * Submits an attempt and calculates the final score.
     *
     * This method is the ONLY place where isCorrect and marksObtained are set.
     * It runs in a single transaction so partial scoring never happens.
     *
     * @param attemptId the attempt to submit
     * @return the updated attempt with score, totalMarks, and percentage
     */
    @Transactional
    public AttemptResponse submitAttempt(Long attemptId) {
        AssessmentAttempt attempt = studentService.findAttemptForCurrentStudent(attemptId);

        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            throw new AssessmentAlreadySubmittedException(
                    "This attempt has already been submitted");
        }

        List<Question> questions = questionRepository
                .findByAssessmentIdOrderByQuestionOrderAsc(attempt.getAssessment().getId());

        // Build a map of existing answers: questionId → StudentAnswer
        Map<Long, StudentAnswer> existingAnswers = studentAnswerRepository
                .findByAttemptId(attemptId)
                .stream()
                .collect(Collectors.toMap(
                        a -> a.getQuestion().getId(),
                        a -> a
                ));

        BigDecimal totalScore = BigDecimal.ZERO;

        for (Question question : questions) {
            StudentAnswer answer = existingAnswers.getOrDefault(
                    question.getId(),
                    createBlankAnswer(attempt, question)   // unanswered question
            );

            // Fetch the correct option for this question
            Optional<Option> correctOption = optionRepository
                    .findByQuestionIdAndIsCorrectTrue(question.getId());

            boolean isCorrect = false;
            BigDecimal marksObtained = BigDecimal.ZERO;

            if (correctOption.isPresent()
                    && answer.getSelectedOption() != null
                    && answer.getSelectedOption().getId().equals(correctOption.get().getId())) {
                isCorrect = true;
                marksObtained = BigDecimal.valueOf(question.getMarks());
            }

            answer.setCorrect(isCorrect);
            answer.setMarksObtained(marksObtained);
            studentAnswerRepository.save(answer);

            totalScore = totalScore.add(marksObtained);
        }

        // Calculate percentage (handle edge case where totalMarks = 0)
        BigDecimal assessmentTotalMarks = BigDecimal.valueOf(
                attempt.getAssessment().getTotalMarks());

        BigDecimal percentage = assessmentTotalMarks.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : totalScore
                        .multiply(BigDecimal.valueOf(100))
                        .divide(assessmentTotalMarks, 2, RoundingMode.HALF_UP);

        // Persist the final results
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationTime = attempt.getExpirationTime();

        // Cap submittedAt to expirationTime if the attempt expired
        LocalDateTime submittedAt = (expirationTime != null && now.isAfter(expirationTime))
                ? expirationTime
                : now;

        attempt.setScore(totalScore);
        attempt.setTotalMarks(assessmentTotalMarks);
        attempt.setPercentage(percentage);
        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(submittedAt);

        AssessmentAttempt submitted = attemptRepository.save(attempt);

        log.info("Attempt submitted: id={}, student={}, score={}/{}, percentage={}%",
                attemptId,
                attempt.getStudent().getUsername(),
                totalScore,
                assessmentTotalMarks,
                percentage);

        return new AttemptResponse(submitted);
    }

    /**
     * Creates a blank (unanswered) StudentAnswer for a question
     * that the student never touched.
     */
    private StudentAnswer createBlankAnswer(AssessmentAttempt attempt, Question question) {
        StudentAnswer blank = new StudentAnswer();
        blank.setAttempt(attempt);
        blank.setQuestion(question);
        blank.setSelectedOption(null);
        blank.setCorrect(false);
        blank.setMarksObtained(BigDecimal.ZERO);
        blank.setAnsweredAt(LocalDateTime.now());
        return blank;
    }
}
