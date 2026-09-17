import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute, RoleRoute } from './routes/ProtectedRoute';

// Auth
import { LoginPage } from './pages/auth/LoginPage';
import { RegisterPage } from './pages/auth/RegisterPage';

// Teacher
import { TeacherDashboard } from './pages/teacher/TeacherDashboard';
import { TeacherAssessmentsPage } from './pages/teacher/TeacherAssessmentsPage';
import { CreateAssessmentPage } from './pages/teacher/CreateAssessmentPage';
import { TeacherAssessmentDetailPage } from './pages/teacher/TeacherAssessmentDetailPage';
import { TeacherAttemptReviewPage } from './pages/teacher/TeacherAttemptReviewPage';

// Student
import { StudentDashboard } from './pages/student/StudentDashboard';
import { StudentAssessmentsPage } from './pages/student/StudentAssessmentsPage';
import { TakeAssessmentPage } from './pages/student/TakeAssessmentPage';
import { StudentHistoryPage } from './pages/student/StudentHistoryPage';
import { StudentAttemptReviewPage } from './pages/student/StudentAttemptReviewPage';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Teacher-only routes */}
          <Route element={<RoleRoute role="TEACHER" />}>
            <Route path="/teacher" element={<TeacherDashboard />} />
            <Route path="/teacher/assessments" element={<TeacherAssessmentsPage />} />
            <Route path="/teacher/assessments/new" element={<CreateAssessmentPage />} />
            <Route path="/teacher/assessments/:id" element={<TeacherAssessmentDetailPage />} />
            <Route path="/teacher/assessments/:assessmentId/attempts/:attemptId" element={<TeacherAttemptReviewPage />} />
          </Route>

          {/* Student-only routes */}
          <Route element={<RoleRoute role="STUDENT" />}>
            <Route path="/student" element={<StudentDashboard />} />
            <Route path="/student/assessments" element={<StudentAssessmentsPage />} />
            <Route path="/student/attempts/:attemptId" element={<TakeAssessmentPage />} />
            <Route path="/student/attempts/:attemptId/review" element={<StudentAttemptReviewPage />} />
            <Route path="/student/history" element={<StudentHistoryPage />} />
          </Route>

          {/* Catch-all: protected redirect to login */}
          <Route element={<ProtectedRoute />}>
            <Route path="/" element={<Navigate to="/login" replace />} />
          </Route>

          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
