import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { studentApi } from '../../api/student';
import { Navbar } from '../../components/Navbar';
import { useAuth } from '../../context/AuthContext';
import type { AssessmentAttempt } from '../../types';
import { BookOpen, TrendingUp, Award, Clock } from 'lucide-react';

export function StudentDashboard() {
  const { user } = useAuth();
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
  const passed = submitted.filter((a) => a.passed);
  const avgPct =
    submitted.length > 0
      ? submitted.reduce((acc, a) => acc + (a.percentage ?? 0), 0) / submitted.length
      : null;

  return (
    <div className="page">
      <Navbar />
      <main className="page-content">
        {/* Welcome */}
        <div style={{ marginBottom: 'var(--space-10)' }}>
          <h1 className="page-title">
            Hello,{' '}
            <span style={{ color: 'var(--color-accent)' }}>{user?.username}</span> 👋
          </h1>
          <p className="page-subtitle">Track your progress and take new assessments.</p>
        </div>

        {/* Stats */}
        <div className="grid grid-3 stagger" style={{ marginBottom: 'var(--space-10)' }}>
          <div className="stat-card">
            <span className="stat-card-label">Completed</span>
            <span className="stat-card-value">{submitted.length}</span>
            <span className="stat-card-sub">Submitted attempts</span>
          </div>
          <div className="stat-card">
            <span className="stat-card-label">Average Score</span>
            <span className="stat-card-value" style={{ color: 'var(--color-accent)' }}>
              {avgPct != null ? `${avgPct.toFixed(1)}%` : '—'}
            </span>
            <span className="stat-card-sub">Across all attempts</span>
          </div>
          <div className="stat-card">
            <span className="stat-card-label">Passed</span>
            <span className="stat-card-value" style={{ color: 'var(--color-success)' }}>
              {passed.length}
            </span>
            <span className="stat-card-sub">of {submitted.length} completed</span>
          </div>
        </div>

        {/* In Progress */}
        {inProgress.length > 0 && (
          <div style={{ marginBottom: 'var(--space-8)' }}>
            <h2 style={{ fontSize: 18, fontWeight: 700, marginBottom: 'var(--space-4)' }}>
              In Progress
            </h2>
            <div className="grid grid-2 stagger">
              {inProgress.map((a) => (
                <Link key={a.id} to={`/student/attempts/${a.id}`} style={{ textDecoration: 'none' }}>
                  <div className="assessment-card">
                    <div className="assessment-card-header">
                      <h3 className="assessment-card-title">{a.assessmentTitle}</h3>
                      <span className="badge badge-in-progress">In Progress</span>
                    </div>
                    <div className="assessment-card-meta">
                      <span className="assessment-card-meta-item">
                        <Clock size={13} /> Started {new Date(a.startedAt).toLocaleDateString()}
                      </span>
                    </div>
                    <button className="btn btn-primary btn-sm">Continue →</button>
                  </div>
                </Link>
              ))}
            </div>
          </div>
        )}

        {/* Quick Links */}
        <div className="page-header" style={{ marginBottom: 'var(--space-5)' }}>
          <h2 style={{ fontSize: 18, fontWeight: 700 }}>Quick Actions</h2>
        </div>
        <div className="grid grid-2 stagger" style={{ marginBottom: 'var(--space-10)' }}>
          <Link to="/student/assessments" style={{ textDecoration: 'none' }}>
            <div className="card card-interactive" style={{ display: 'flex', gap: 'var(--space-4)', alignItems: 'center' }}>
              <div style={{ width: 44, height: 44, borderRadius: 'var(--radius-md)', background: 'var(--color-accent-dim)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <BookOpen size={20} color="var(--color-accent)" />
              </div>
              <div>
                <div style={{ fontWeight: 700, marginBottom: 2 }}>Browse Assessments</div>
                <div style={{ fontSize: 13, color: 'var(--text-secondary)' }}>Find and start new assessments</div>
              </div>
            </div>
          </Link>
          <Link to="/student/history" style={{ textDecoration: 'none' }}>
            <div className="card card-interactive" style={{ display: 'flex', gap: 'var(--space-4)', alignItems: 'center' }}>
              <div style={{ width: 44, height: 44, borderRadius: 'var(--radius-md)', background: 'rgba(34,197,94,0.12)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <TrendingUp size={20} color="var(--color-success)" />
              </div>
              <div>
                <div style={{ fontWeight: 700, marginBottom: 2 }}>My History</div>
                <div style={{ fontSize: 13, color: 'var(--text-secondary)' }}>Review past attempts</div>
              </div>
            </div>
          </Link>
        </div>

        {/* Recent Results */}
        {!loading && submitted.length > 0 && (
          <div>
            <div className="page-header" style={{ marginBottom: 'var(--space-5)' }}>
              <h2 style={{ fontSize: 18, fontWeight: 700 }}>Recent Results</h2>
              <Link to="/student/history" className="btn btn-ghost btn-sm">View all</Link>
            </div>
            <div className="grid stagger" style={{ gridTemplateColumns: '1fr' }}>
              {submitted.slice(0, 4).map((a) => (
                <ResultRow key={a.id} attempt={a} />
              ))}
            </div>
          </div>
        )}

        {loading && (
          <div className="loading-screen" style={{ minHeight: 200 }}>
            <div className="spinner" />
          </div>
        )}
      </main>
    </div>
  );
}

function ResultRow({ attempt: a }: { attempt: AssessmentAttempt }) {
  const pct = a.percentage ?? 0;
  return (
    <Link to={`/student/attempts/${a.id}/review`} style={{ textDecoration: 'none' }}>
      <div className="card card-interactive">
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-4)' }}>
          <div style={{ width: 48, height: 48, borderRadius: 'var(--radius-md)', background: a.passed ? 'var(--color-success-dim)' : 'var(--color-error-dim)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
            <Award size={22} color={a.passed ? 'var(--color-success)' : 'var(--color-error)'} />
          </div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontWeight: 700, fontSize: 15, marginBottom: 4, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
              {a.assessmentTitle}
            </div>
            <div style={{ fontSize: 13, color: 'var(--text-muted)' }}>
              {new Date(a.submittedAt!).toLocaleDateString()}
            </div>
          </div>
          <div style={{ textAlign: 'right', flexShrink: 0 }}>
            <div style={{ fontWeight: 800, fontSize: 18, color: a.passed ? 'var(--color-success)' : 'var(--color-error)' }}>
              {pct.toFixed(1)}%
            </div>
            <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>{a.score}/{a.totalMarks} marks</div>
          </div>
        </div>
      </div>
    </Link>
  );
}
