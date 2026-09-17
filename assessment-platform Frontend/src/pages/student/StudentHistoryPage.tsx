import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { studentApi } from '../../api/student';
import { Navbar } from '../../components/Navbar';
import type { AssessmentAttempt } from '../../types';
import { Award, Clock, ChevronRight } from 'lucide-react';

export function StudentHistoryPage() {
  const [attempts, setAttempts] = useState<AssessmentAttempt[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    studentApi.getMyAttempts()
      .then((r) => setAttempts(r.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const submitted = attempts.filter((a) => a.status === 'SUBMITTED');
  const inProgress = attempts.filter((a) => a.status === 'IN_PROGRESS');

  return (
    <div className="page">
      <Navbar />
      <main className="page-content">
        <div className="page-header">
          <div>
            <h1 className="page-title">My History</h1>
            <p className="page-subtitle">All your assessment attempts</p>
          </div>
        </div>

        {loading ? (
          <div className="loading-screen" style={{ minHeight: 300 }}>
            <div className="spinner" />
          </div>
        ) : attempts.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon"><Award size={28} /></div>
            <p className="empty-state-title">No attempts yet</p>
            <p className="empty-state-desc">Start an assessment to see your results here.</p>
            <Link to="/student/assessments" className="btn btn-primary">Browse Assessments</Link>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-8)' }}>
            {inProgress.length > 0 && (
              <div>
                <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 'var(--space-4)', color: 'var(--color-accent)' }}>
                  In Progress
                </h2>
                <div className="grid stagger" style={{ gridTemplateColumns: '1fr' }}>
                  {inProgress.map((a) => (
                    <Link key={a.id} to={`/student/attempts/${a.id}`} style={{ textDecoration: 'none' }}>
                      <div className="card card-interactive" style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-4)' }}>
                        <div style={{ width: 44, height: 44, borderRadius: 'var(--radius-md)', background: 'var(--color-accent-dim)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                          <Clock size={20} color="var(--color-accent)" />
                        </div>
                        <div style={{ flex: 1 }}>
                          <div style={{ fontWeight: 700, fontSize: 15 }}>{a.assessmentTitle}</div>
                          <div style={{ fontSize: 13, color: 'var(--text-muted)' }}>
                            Started {new Date(a.startedAt).toLocaleString()}
                          </div>
                        </div>
                        <span className="badge badge-in-progress">In Progress</span>
                        <ChevronRight size={16} color="var(--text-muted)" />
                      </div>
                    </Link>
                  ))}
                </div>
              </div>
            )}

            {submitted.length > 0 && (
              <div>
                <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 'var(--space-4)', color: 'var(--text-secondary)' }}>
                  Completed
                </h2>
                <div className="grid stagger" style={{ gridTemplateColumns: '1fr' }}>
                  {submitted.map((a) => (
                    <Link key={a.id} to={`/student/attempts/${a.id}/review`} style={{ textDecoration: 'none' }}>
                      <div className="card card-interactive" style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-4)' }}>
                        <div style={{
                          width: 44, height: 44, borderRadius: 'var(--radius-md)',
                          background: a.passed ? 'var(--color-success-dim)' : 'var(--color-error-dim)',
                          display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0
                        }}>
                          <Award size={20} color={a.passed ? 'var(--color-success)' : 'var(--color-error)'} />
                        </div>
                        <div style={{ flex: 1 }}>
                          <div style={{ fontWeight: 700, fontSize: 15 }}>{a.assessmentTitle}</div>
                          <div style={{ fontSize: 13, color: 'var(--text-muted)' }}>
                            Submitted {new Date(a.submittedAt!).toLocaleDateString()}
                          </div>
                        </div>
                        <div style={{ textAlign: 'right', flexShrink: 0 }}>
                          <div style={{ fontWeight: 800, fontSize: 18, color: a.passed ? 'var(--color-success)' : 'var(--color-error)' }}>
                            {(a.percentage ?? 0).toFixed(1)}%
                          </div>
                          <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                            {a.score}/{a.totalMarks}
                          </div>
                        </div>
                        <ChevronRight size={16} color="var(--text-muted)" />
                      </div>
                    </Link>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  );
}
