import api from './axios';
import type {
  Assessment,
  CreateAssessmentRequest,
  Question,
  Option,
  CreateQuestionRequest,
  TeacherAttemptSummary,
  AttemptResultResponse,
} from '../types';

export const teacherApi = {
  // ── Assessments ────────────────────────────────────────────────────────────
  getMyAssessments: () =>
    api.get<Assessment[]>('/api/teacher/assessments'),

  createAssessment: (data: CreateAssessmentRequest) =>
    api.post<Assessment>('/api/teacher/assessments', data),

  updateAssessment: (id: number, data: CreateAssessmentRequest) =>
    api.put<Assessment>(`/api/teacher/assessments/${id}`, data),

  deleteAssessment: (id: number) =>
    api.delete(`/api/teacher/assessments/${id}`),

  publishAssessment: (id: number) =>
    api.post<Assessment>(`/api/teacher/assessments/${id}/publish`),

  // ── Questions ──────────────────────────────────────────────────────────────
  // FIX #1: GET /api/teacher/assessments/{id}/questions — newly added backend endpoint
  getQuestions: (assessmentId: number) =>
    api.get<Question[]>(`/api/teacher/assessments/${assessmentId}/questions`),

  addQuestion: (assessmentId: number, data: CreateQuestionRequest) =>
    api.post<Question>(`/api/teacher/assessments/${assessmentId}/questions`, data),

  addOption: (questionId: number, data: { optionText: string; optionOrder: number; isCorrect: boolean }) =>
    api.post<Option>(`/api/teacher/questions/${questionId}/options`, data),

  // FIX #2: correct path is DELETE /api/teacher/questions/{questionId}
  // NOT /api/teacher/assessments/{id}/questions/{qId}
  deleteQuestion: (questionId: number) =>
    api.delete(`/api/teacher/questions/${questionId}`),

  // ── Results ────────────────────────────────────────────────────────────────
  getAttempts: (assessmentId: number) =>
    api.get<TeacherAttemptSummary[]>(`/api/teacher/assessments/${assessmentId}/attempts`),

  // FIX #3: backend has no combined review endpoint — make 2 calls and merge
  // GET /api/teacher/attempts/{attemptId}        → summary
  // GET /api/teacher/attempts/{attemptId}/answers → answer list
  getAttemptSummary: (attemptId: number) =>
    api.get<TeacherAttemptSummary>(`/api/teacher/attempts/${attemptId}`),

  getAttemptAnswers: (attemptId: number) =>
    api.get<AttemptResultResponse['answers']>(`/api/teacher/attempts/${attemptId}/answers`),
};
