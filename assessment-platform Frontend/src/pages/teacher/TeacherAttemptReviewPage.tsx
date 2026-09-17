import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { teacherApi } from '../../api/teacher';
import { Navbar } from '../../components/Navbar';
import type { TeacherAttemptSummary, StudentAnswerResponse } from '../../types';
import { ArrowLeft, CheckCircle2, XCircle, MinusCircle } from 'lucide-react';

// FIX: backend has no combined review endpoint.
// We make 2 calls: GET /api/teacher/attempts/{id}  +  GET /api/teacher/attempts/{id}/answers
export function TeacherAttemptReviewPage() {
  const { attemptId } = useParams<{ attemptId: string }>();
  const navigate = useNavigate();

  const [summary, setSummary] = useState<TeacherAttemptSummary | null>(null);
  const [answers, setAnswers] = useState<StudentAnswerResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const aId = Number(attemptId);
    Promise.all([
      teacherApi.getAttemptSummary(aId),
      teacherApi.getAttemptAnswers(aId),
    ])
      .then(([summaryRes, answersRes]) => {
        setSummary(summaryRes.data);
        setAnswers(answersRes.data);
      })
      .catch(() => navigate(-1))
      .finally(() => setLoading(false));
  }, [attemptId]);

  if (loading) return <div className="page"><Navbar /><div className="loading-screen"><div className="spinner" /></div></div>;
  if (!summary) return null;

  const pct = summary.percentage != null ? Number(summary.percentage) : 0;
  const passed = pct >= 60;

  return (
    <div className="page">
      <Navbar />
      <main className="page-content" style={{ maxWidth: 780 }}>
        <button className="btn btn-ghost" onClick={() => navigate(-1)} style={{ marginBottom: 'var(--space-6)' }}>
          <ArrowLeft size={16} /> Back
        </button>

        {/* Result Hero */}
        <div className="card" style={{ textAlign: 'center', padding: 'var(--space-10)', marginBottom: 'var(--space-6)' }}>
          <div
            className="result-score-ring"
            style={{ '--pct': pct } as React.CSSProperties}
          >
            <div className="result-score-inner">
              <div className="result-score-pct">{pct.toFixed(1)}%</div>
              <div className="result-score-label">Score</div>
            </div>
          </div>

          <h1 style={{ fontSize: 22, fontWeight: 800, marginBottom: 'var(--space-2)' }}>
            {summary.assessmentTitle}
          </h1>
          <p style={{ color: 'var(--text-secondary)', marginBottom: 'var(--space-4)' }}>
            Student: <strong style={{ color: 'var(--text-primary)' }}>{summary.studentUsername}</strong>
          </p>

          <div style={{ display: 'flex', gap: 'var(--space-4)', justifyContent: 'center', flexWrap: 'wrap' }}>
            <Chip label="Score" value={`${summary.score ?? 0}/${summary.totalMarks ?? 0}`} />
            <Chip label="Percentage" value={`${pct.toFixed(1)}%`} />
            <Chip
              label="Result"
              value={passed ? 'Passed' : 'Failed'}
              color={passed ? 'var(--color-success)' : 'var(--color-error)'}
            />
          </div>
        </div>

        {/* Answers */}
        <h2 style={{ fontSize: 18, fontWeight: 700, marginBottom: 'var(--space-5)' }}>Answer Review</h2>
        <div className="grid stagger" style={{ gridTemplateColumns: '1fr' }}>
          {answers.map((ans, idx) => {
            const correct = ans.isCorrect === true;
            const wrong = ans.isCorrect === false && !!ans.selectedOptionId;

            return (
              <div
                key={ans.questionId}
                className="card"
                style={{
                  borderColor: correct
                    ? 'rgba(34,197,94,0.3)'
                    : wrong
                    ? 'rgba(239,68,68,0.3)'
                    : 'var(--color-border)',
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 'var(--space-4)' }}>
                  <div style={{ flex: 1 }}>
                    <p style={{ fontSize: 13, color: 'var(--color-accent)', fontWeight: 600, marginBottom: 'var(--space-2)' }}>
                      Q{idx + 1}
                    </p>
                    <p style={{ fontSize: 16, fontWeight: 600, marginBottom: 'var(--space-4)' }}>{ans.questionText}</p>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)', fontSize: 14 }}>
                      {ans.selectedOptionText && (
                        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', color: correct ? 'var(--color-success)' : 'var(--color-error)' }}>
                          {correct ? <CheckCircle2 size={15} /> : <XCircle size={15} />}
                          <span>Student answered: <strong>{ans.selectedOptionText}</strong></span>
                        </div>
                      )}
                      {!ans.selectedOptionText && (
                        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', color: 'var(--text-muted)' }}>
                          <MinusCircle size={15} />
                          <span>No answer given</span>
                        </div>
                      )}
                      {ans.correctOptionText && !correct && (
                        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', color: 'var(--color-success)' }}>
                          <CheckCircle2 size={15} />
                          <span>Correct answer: <strong>{ans.correctOptionText}</strong></span>
                        </div>
                      )}
                    </div>
                  </div>

                  <div style={{
                    flexShrink: 0,
                    textAlign: 'right',
                    fontSize: 15,
                    fontWeight: 700,
                    color: correct ? 'var(--color-success)' : wrong ? 'var(--color-error)' : 'var(--text-muted)',
                  }}>
                    {/* FIX: backend sends marksObtained, not pointsEarned */}
                    +{ans.marksObtained}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </main>
    </div>
  );
}

function Chip({ label, value, color }: { label: string; value: string; color?: string }) {
  return (
    <div style={{
      background: 'var(--color-surface-2)',
      border: '1px solid var(--color-border)',
      borderRadius: 'var(--radius-md)',
      padding: '10px 18px',
      textAlign: 'center',
    }}>
      <div style={{ fontSize: 11, color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.06em' }}>
        {label}
      </div>
      <div style={{ fontSize: 18, fontWeight: 800, color: color ?? 'var(--text-primary)', marginTop: 2 }}>
        {value}
      </div>
    </div>
  );
}
