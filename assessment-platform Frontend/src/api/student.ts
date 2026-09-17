import api from './axios';
import type {
  Assessment,
  AttemptWithQuestionsResponse,
  SaveAnswerRequest,
  AssessmentAttempt,
  AttemptResultResponse,
} from '../types';

export const studentApi = {
  // ── Browse published assessments ───────────────────────────────────────────
  getPublishedAssessments: () =>
    api.get<Assessment[]>('/api/student/assessments'),

  // ── Start an attempt ───────────────────────────────────────────────────────
  startAttempt: (assessmentId: number) =>
    api.post<AttemptWithQuestionsResponse>(`/api/student/assessments/${assessmentId}/start`),

  // ── Get questions for an in-progress attempt (no correct answers shown) ────
  getAttemptQuestions: (attemptId: number) =>
    api.get<AttemptWithQuestionsResponse>(`/api/student/attempts/${attemptId}`),

  // ── Save / update an answer ────────────────────────────────────────────────
  saveAnswer: (attemptId: number, questionId: number, data: SaveAnswerRequest) =>
    api.put(`/api/student/attempts/${attemptId}/answers/${questionId}`, data),

  // ── Submit attempt ─────────────────────────────────────────────────────────
  submitAttempt: (attemptId: number) =>
    api.post<AssessmentAttempt>(`/api/student/attempts/${attemptId}/submit`),

  // ── My past attempts ───────────────────────────────────────────────────────
  getMyAttempts: () =>
    api.get<AssessmentAttempt[]>('/api/student/attempts'),

  // FIX #4: backend endpoint is /result NOT /review
  getAttemptResult: (attemptId: number) =>
    api.get<AttemptResultResponse>(`/api/student/attempts/${attemptId}/result`),
};
