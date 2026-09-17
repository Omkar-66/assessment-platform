import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { studentApi } from '../../api/student';
import { Navbar } from '../../components/Navbar';
import type { AttemptResultResponse } from '../../types';
import { ArrowLeft, CheckCircle2, XCircle, MinusCircle, Home } from 'lucide-react';

export function StudentAttemptReviewPage() {
  const { attemptId } = useParams<{ attemptId: string }>();
  const navigate = useNavigate();
  const [review, setReview] = useState<AttemptResultResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    // FIX: backend endpoint is /result not /review
    studentApi.getAttemptResult(Number(attemptId))
      .then((r) => setReview(r.data))
      .catch((err) => {
        setError(err?.response?.data?.message || 'Failed to load review.');
      })
      .finally(() => setLoading(false));
  }, [attemptId]);

  if (loading) return <div className="page"><Navbar /><div className="loading-screen"><div className="spinner" /></div></div>;

  if (error) {
    return (
      <div className="page">
        <Navbar />
        <main className="page-content" style={{ maxWidth: 640 }}>
          <div className="alert alert-error">{error}</div>
          <button className="btn btn-ghost" onClick={() => navigate(-1)} style={{ marginTop: 'var(--space-4)' }}>
            <ArrowLeft size={16} /> Go back
          </button>
        </main>
      </div>
    );
  }

  if (!review) return null;

  const pct = review.percentage != null ? Number(review.percentage) : 0;
  // FIX: backend has no 'passed' field — compute from percentage
  const passed = pct >= 60;
  const fillColor = passed ? 'var(--color-success)' : 'var(--color-error)';

  return (
    <div className="page">
      <Navbar />
      <main className="page-content" style={{ maxWidth: 780 }}>
        <button className="btn btn-ghost" onClick={() => navigate('/student/history')} style={{ marginBottom: 'var(--space-6)' }}>
          <ArrowLeft size={16} /> Back to History
        </button>

        {/* Result Hero */}
        <div className="card" style={{ textAlign: 'center', padding: 'var(--space-10)', marginBottom: 'var(--space-6)' }}>
          <div
            className="result-score-ring"
            style={{
              '--pct': pct,
              background: `conic-gradient(${fillColor} calc(${pct} * 1%), var(--color-surface-2) 0)`,
            } as React.CSSProperties}
          >
            <div className="result-score-inner">
              <div className="result-score-pct" style={{ color: fillColor }}>{pct.toFixed(1)}%</div>
              <div className="result-score-label">Score</div>
            </div>
          </div>

          <h1 style={{ fontSize: 22, fontWeight: 800, marginBottom: 'var(--space-2)' }}>
            {review.assessmentTitle}
          </h1>

          <div style={{ display: 'flex', gap: 'var(--space-4)', justifyContent: 'center', flexWrap: 'wrap', marginBottom: 'var(--space-6)' }}>
            <Chip label="Score" value={`${review.score}/${review.totalMarks}`} />
            <Chip label="Percentage" value={`${pct.toFixed(1)}%`} />
            <Chip
              label="Result"
              value={passed ? '🎉 Passed' : '❌ Failed'}
              color={passed ? 'var(--color-success)' : 'var(--color-error)'}
            />
          </div>

          <div style={{ display: 'flex', gap: 'var(--space-3)', justifyContent: 'center' }}>
            <Link to="/student/assessments" className="btn btn-primary btn-sm">
              <Home size={14} /> More Assessments
            </Link>
            <Link to="/student/history" className="btn btn-secondary btn-sm">
              History
            </Link>
          </div>
        </div>

        {/* Answer Breakdown */}
        <h2 style={{ fontSize: 18, fontWeight: 700, marginBottom: 'var(--space-5)' }}>Answer Breakdown</h2>
        <div className="grid stagger" style={{ gridTemplateColumns: '1fr' }}>
          {review.answers.map((ans, idx) => {
            const correct = ans.isCorrect === true;
            const wrong = ans.isCorrect === false;

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
                      {ans.selectedOptionText ? (
                        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', color: correct ? 'var(--color-success)' : 'var(--color-error)' }}>
                          {correct ? <CheckCircle2 size={15} /> : <XCircle size={15} />}
                          <span>Your answer: <strong>{ans.selectedOptionText}</strong></span>
                        </div>
                      ) : (
                        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', color: 'var(--text-muted)' }}>
                          <MinusCircle size={15} />
                          <span>No answer given</span>
                        </div>
                      )}

                      {!correct && ans.correctOptionText && (
                        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', color: 'var(--color-success)' }}>
                          <CheckCircle2 size={15} />
                          <span>Correct answer: <strong>{ans.correctOptionText}</strong></span>
                        </div>
                      )}
                    </div>
                  </div>

                  <div style={{
                    flexShrink: 0, textAlign: 'right', fontSize: 15, fontWeight: 700,
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
