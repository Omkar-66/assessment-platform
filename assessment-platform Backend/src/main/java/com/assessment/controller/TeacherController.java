package com.assessment.controller;

import com.assessment.dto.*;
import com.assessment.service.OptionService;
import com.assessment.service.QuestionService;
import com.assessment.service.TeacherAssessmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for teacher assessment management.
 *
 * All endpoints are protected by Spring Security: only authenticated
 * users with ROLE_TEACHER can reach /api/teacher/**.
 *
 * This controller is intentionally thin — all business logic lives in
 * the service layer. Controllers only handle HTTP concerns.
 *
 * Endpoints:
 *
 * Assessment CRUD:
 *   POST   /api/teacher/assessments
 *   GET    /api/teacher/assessments
 *   GET    /api/teacher/assessments/{id}
 *   PUT    /api/teacher/assessments/{id}
 *   DELETE /api/teacher/assessments/{id}
 *
 * Publish / Close:
 *   POST   /api/teacher/assessments/{id}/publish
 *   POST   /api/teacher/assessments/{id}/close
 *
 * Questions:
 *   POST   /api/teacher/assessments/{assessmentId}/questions
 *   PUT    /api/teacher/questions/{questionId}
 *   DELETE /api/teacher/questions/{questionId}
 *
 * Options:
 *   POST   /api/teacher/questions/{questionId}/options
 *   PUT    /api/teacher/options/{optionId}
 *   DELETE /api/teacher/options/{optionId}
 *
 * Results:
 *   GET    /api/teacher/assessments/{id}/attempts
 *   GET    /api/teacher/attempts/{attemptId}
 *   GET    /api/teacher/attempts/{attemptId}/answers
 */
@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private final TeacherAssessmentService assessmentService;
    private final QuestionService questionService;
    private final OptionService optionService;

    public TeacherController(TeacherAssessmentService assessmentService,
                             QuestionService questionService,
                             OptionService optionService) {
        this.assessmentService = assessmentService;
        this.questionService = questionService;
        this.optionService = optionService;
    }

    // ── Assessment CRUD ──────────────────────────────────────────────────

    @PostMapping("/assessments")
    public ResponseEntity<AssessmentResponse> createAssessment(
            @Valid @RequestBody AssessmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assessmentService.createAssessment(request));
    }

    @GetMapping("/assessments")
    public ResponseEntity<List<AssessmentResponse>> getMyAssessments() {
        return ResponseEntity.ok(assessmentService.getMyAssessments());
    }

    @GetMapping("/assessments/{id}")
    public ResponseEntity<AssessmentResponse> getAssessmentById(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.getAssessmentById(id));
    }

    @PutMapping("/assessments/{id}")
    public ResponseEntity<AssessmentResponse> updateAssessment(
            @PathVariable Long id,
            @Valid @RequestBody AssessmentRequest request) {
        return ResponseEntity.ok(assessmentService.updateAssessment(id, request));
    }

    @DeleteMapping("/assessments/{id}")
    public ResponseEntity<Void> deleteAssessment(@PathVariable Long id) {
        assessmentService.deleteAssessment(id);
        return ResponseEntity.noContent().build();
    }

    // ── Publish / Close ──────────────────────────────────────────────────

    @PostMapping("/assessments/{id}/publish")
    public ResponseEntity<AssessmentResponse> publishAssessment(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.publishAssessment(id));
    }

    @PostMapping("/assessments/{id}/close")
    public ResponseEntity<AssessmentResponse> closeAssessment(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.closeAssessment(id));
    }

    // ── Questions ────────────────────────────────────────────────────────

    @PostMapping("/assessments/{assessmentId}/questions")
    public ResponseEntity<QuestionResponse> addQuestion(
            @PathVariable Long assessmentId,
            @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.addQuestion(assessmentId, request));
    }

    @GetMapping("/assessments/{assessmentId}/questions")
    public ResponseEntity<List<QuestionResponse>> getQuestions(
            @PathVariable Long assessmentId) {
        return ResponseEntity.ok(questionService.getQuestionsForAssessment(assessmentId));
    }

    @PutMapping("/questions/{questionId}")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(questionService.updateQuestion(questionId, request));
    }

    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        questionService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    // ── Options ──────────────────────────────────────────────────────────

    @PostMapping("/questions/{questionId}/options")
    public ResponseEntity<OptionResponse> addOption(
            @PathVariable Long questionId,
            @Valid @RequestBody OptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(optionService.addOption(questionId, request));
    }

    @PutMapping("/options/{optionId}")
    public ResponseEntity<OptionResponse> updateOption(
            @PathVariable Long optionId,
            @Valid @RequestBody OptionRequest request) {
        return ResponseEntity.ok(optionService.updateOption(optionId, request));
    }

    @DeleteMapping("/options/{optionId}")
    public ResponseEntity<Void> deleteOption(@PathVariable Long optionId) {
        optionService.deleteOption(optionId);
        return ResponseEntity.noContent().build();
    }

    // ── Results ──────────────────────────────────────────────────────────

    @GetMapping("/assessments/{id}/attempts")
    public ResponseEntity<List<AttemptResponse>> getAttemptsForAssessment(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.getAttemptsForAssessment(id));
    }

    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<AttemptResponse> getAttemptDetail(@PathVariable Long attemptId) {
        return ResponseEntity.ok(assessmentService.getAttemptDetail(attemptId));
    }

    @GetMapping("/attempts/{attemptId}/answers")
    public ResponseEntity<List<StudentAnswerResponse>> getAnswersForAttempt(
            @PathVariable Long attemptId) {
        return ResponseEntity.ok(assessmentService.getAnswersForAttempt(attemptId));
    }
}
