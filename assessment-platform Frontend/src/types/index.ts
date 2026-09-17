// ─── Auth ────────────────────────────────────────────────────────────────────

export interface AuthUser {
  id: number;
  username: string;
  email: string;
  role: 'TEACHER' | 'STUDENT';
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  role: 'TEACHER' | 'STUDENT';
}

// Matches AuthResponse.java exactly
export interface AuthResponse {
  token: string;
  tokenType: string;
  userId: number;
  username: string;
  email: string;
  role: string;
}

// ─── Assessment ───────────────────────────────────────────────────────────────

export type AssessmentStatus = 'DRAFT' | 'PUBLISHED' | 'CLOSED';

// Matches AssessmentResponse.java exactly — backend has NO passingScore field
export interface Assessment {
  id: number;
  title: string;
  description: string;
  durationMinutes: number;
  totalMarks: number;
  status: AssessmentStatus;
  createdById: number;
  createdByUsername: string;
  createdAt: string;
  updatedAt?: string;
  passingScore?: number;
  questionCount?: number;
}

// Matches QuestionResponse.java exactly
export interface Question {
  id: number;
  assessmentId: number;
  questionText: string;
  marks: number;
  questionOrder?: number;
  options: Option[];
}

// Matches OptionResponse.java (isCorrect only populated for teacher view)
export interface Option {
  id: number;
  optionText: string;
  isCorrect?: boolean;
}

// ─── Attempts ─────────────────────────────────────────────────────────────────

export type AttemptStatus = 'IN_PROGRESS' | 'SUBMITTED';

// Matches AttemptResponse.java exactly — backend has NO 'passed' field
export interface AssessmentAttempt {
  id: number;
  assessmentId: number;
  assessmentTitle: string;
  studentId: number;
  studentUsername: string;
  score?: number;
  totalMarks?: number;
  percentage?: number;
  status: AttemptStatus;
  startedAt: string;
  submittedAt?: string;
  passed?: boolean;
}

// Matches AttemptWithQuestionsResponse.java exactly
export interface AttemptWithQuestionsResponse {
  attemptId: number;
  status: string;
  startedAt: string;
  assessmentId: number;
  assessmentTitle: string;
  assessmentDescription: string;
  durationMinutes: number;
  totalMarks: number;
  questions: Question[];
}

export interface SaveAnswerRequest {
  selectedOptionId: number;
}

// ─── Student Result ───────────────────────────────────────────────────────────

// Matches StudentAnswerResponse.java exactly — field is marksObtained (NOT pointsEarned)
export interface StudentAnswerResponse {
  id: number;
  questionId: number;
  questionText: string;
  selectedOptionId?: number;
  selectedOptionText?: string;
  correctOptionId?: number;
  correctOptionText?: string;
  isCorrect: boolean;
  marksObtained: number;
  answeredAt: string;
}

// Matches AttemptResultResponse.java exactly — GET /api/student/attempts/{id}/result
export interface AttemptResultResponse {
  attemptId: number;
  assessmentId: number;
  assessmentTitle: string;
  score: number;
  totalMarks: number;
  percentage: number;
  status: string;
  startedAt: string;
  submittedAt: string;
  answers: StudentAnswerResponse[];
}

// ─── Teacher ──────────────────────────────────────────────────────────────────

// Matches AssessmentRequest.java — no passingScore field in backend
export interface CreateAssessmentRequest {
  title: string;
  description: string;
  durationMinutes: number;
}

export interface CreateQuestionRequest {
  questionText: string;
  marks: number;
  questionOrder: number;
  options?: CreateOptionRequest[];
}

export interface CreateOptionRequest {
  optionText: string;
  isCorrect: boolean;
}

// Matches AttemptResponse.java — field is 'id' NOT 'attemptId'
export interface TeacherAttemptSummary {
  id: number;
  assessmentId: number;
  assessmentTitle: string;
  studentId: number;
  studentUsername: string;
  score?: number;
  totalMarks?: number;
  percentage?: number;
  status: AttemptStatus;
  startedAt: string;
  submittedAt?: string;
}
