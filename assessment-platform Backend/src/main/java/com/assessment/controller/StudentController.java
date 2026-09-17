package com.assessment.controller;

import com.assessment.dto.*;
import com.assessment.service.AnswerService;
import com.assessment.service.ScoringService;
import com.assessment.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for the student assessment flow.
 *
 * All endpoints are under /api/student/** and are restricted to ROLE_STUDENT.
 *
 * Endpoints:
 *
 * Browse:
 *   GET  /api/student/assessments           → list PUBLISHED assessments
 *   GET  /api/student/assessments/{id}      → view one assessment (metadata only)
 *
 * Attempt:
 *   POST /api/student/assessments/{id}/start   → start attempt, get questions
 *   GET  /api/student/attempts/{id}            → view in-progress attempt + questions
 *   GET  /api/student/attempts                 → list own attempts
 *
 * Answers:
 *   POST /api/student/attempts/{id}/answers                    → save answer
 *   PUT  /api/student/attempts/{id}/answers/{questionId}       → update answer
 *
 * Submit & Result:
 *   POST /api/student/attempts/{id}/submit     → submit + trigger scoring
 *   GET  /api/student/attempts/{id}/result     → view score and breakdown
 */
@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;
    private final AnswerService answerService;
    private final ScoringService scoringService;

    public StudentController(StudentService studentService,
                             AnswerService answerService,
                             ScoringService scoringService) {
        this.studentService = studentService;
        this.answerService = answerService;
        this.scoringService = scoringService;
    }

    // ── Browse ───────────────────────────────────────────────────────────

    @GetMapping("/assessments")
    public ResponseEntity<List<AssessmentResponse>> getPublishedAssessments() {
        return ResponseEntity.ok(studentService.getPublishedAssessments());
    }

    @GetMapping("/assessments/{id}")
    public ResponseEntity<AssessmentResponse> getAssessmentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getPublishedAssessmentById(id));
    }

    // ── Attempt ──────────────────────────────────────────────────────────

    @PostMapping("/assessments/{assessmentId}/start")
    public ResponseEntity<AttemptWithQuestionsResponse> startAttempt(
            @PathVariable Long assessmentId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studentService.startAttempt(assessmentId));
    }

    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<AttemptWithQuestionsResponse> getAttemptWithQuestions(
            @PathVariable Long attemptId) {
        return ResponseEntity.ok(studentService.getAttemptWithQuestions(attemptId));
    }

    @GetMapping("/attempts")
    public ResponseEntity<List<AttemptResponse>> getMyAttempts() {
        return ResponseEntity.ok(studentService.getMyAttempts());
    }

    // ── Answers ──────────────────────────────────────────────────────────

    /**
     * Saves an answer for a question in an in-progress attempt.
     * questionId must be included in the request body.
     */
    @PostMapping("/attempts/{attemptId}/answers")
    public ResponseEntity<StudentAnswerResponse> saveAnswer(
            @PathVariable Long attemptId,
            @RequestBody AnswerRequest request) {

        if (request.getQuestionId() == null) {
            return ResponseEntity.badRequest().build();
        }

        StudentAnswerResponse response = answerService.saveOrUpdateAnswer(
                attemptId, request.getQuestionId(), request.getSelectedOptionId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates the answer for a specific question (questionId from path variable).
     * selectedOptionId can be null to clear/skip the answer.
     */
    @PutMapping("/attempts/{attemptId}/answers/{questionId}")
    public ResponseEntity<StudentAnswerResponse> updateAnswer(
            @PathVariable Long attemptId,
            @PathVariable Long questionId,
            @RequestBody AnswerRequest request) {

        StudentAnswerResponse response = answerService.saveOrUpdateAnswer(
                attemptId, questionId, request.getSelectedOptionId());
        return ResponseEntity.ok(response);
    }

    // ── Submit & Result ───────────────────────────────────────────────────

    @PostMapping("/attempts/{attemptId}/submit")
    public ResponseEntity<AttemptResponse> submitAttempt(@PathVariable Long attemptId) {
        return ResponseEntity.ok(scoringService.submitAttempt(attemptId));
    }

    @GetMapping("/attempts/{attemptId}/result")
    public ResponseEntity<AttemptResultResponse> getAttemptResult(
            @PathVariable Long attemptId) {
        return ResponseEntity.ok(studentService.getAttemptResult(attemptId));
    }
}
