import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { teacherApi } from '../../api/teacher';
import { Navbar } from '../../components/Navbar';
import { useAuth } from '../../context/AuthContext';
import type { Assessment } from '../../types';
import { BookOpen, PlusCircle, Users, Clock, CheckCircle } from 'lucide-react';

export function TeacherDashboard() {
  const { user } = useAuth();
  const [assessments, setAssessments] = useState<Assessment[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    teacherApi.getMyAssessments()
      .then((r) => setAssessments(r.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const published = assessments.filter((a) => a.status === 'PUBLISHED');
  const drafts = assessments.filter((a) => a.status === 'DRAFT');

  return (
    <div className="page">
      <Navbar />
      <main className="page-content">
        {/* Welcome */}
        <div style={{ marginBottom: 'var(--space-10)' }}>
          <h1 className="page-title">
            Good {getGreeting()},{' '}
            <span style={{ color: 'var(--color-accent)' }}>{user?.username}</span> 👋
          </h1>
          <p className="page-subtitle">
            Manage your assessments and track student performance.
          </p>
        </div>

        {/* Stats */}
        <div className="grid grid-3 stagger" style={{ marginBottom: 'var(--space-10)' }}>
          <div className="stat-card">
            <span className="stat-card-label">Total Assessments</span>
            <span className="stat-card-value">{assessments.length}</span>
            <span className="stat-card-sub">All time</span>
          </div>
          <div className="stat-card">
            <span className="stat-card-label">Published</span>
            <span className="stat-card-value" style={{ color: 'var(--color-success)' }}>
              {published.length}
            </span>
            <span className="stat-card-sub">Live &amp; accessible to students</span>
          </div>
          <div className="stat-card">
            <span className="stat-card-label">Drafts</span>
            <span className="stat-card-value" style={{ color: 'var(--color-warning)' }}>
              {drafts.length}
            </span>
            <span className="stat-card-sub">Not yet published</span>
          </div>
        </div>

        {/* Recent Assessments */}
        <div className="page-header">
          <div>
            <h2 style={{ fontSize: 20, fontWeight: 700 }}>Your Assessments</h2>
            <p className="page-subtitle">
              {loading ? 'Loading…' : `${assessments.length} total`}
            </p>
          </div>
          <Link to="/teacher/assessments/new" className="btn btn-primary">
            <PlusCircle size={16} />
            New Assessment
          </Link>
        </div>

        {loading ? (
          <div className="loading-screen" style={{ minHeight: 200 }}>
            <div className="spinner" />
          </div>
        ) : assessments.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">
              <BookOpen size={28} />
            </div>
            <p className="empty-state-title">No assessments yet</p>
            <p className="empty-state-desc">
              Create your first assessment to get started.
            </p>
            <Link to="/teacher/assessments/new" className="btn btn-primary">
              <PlusCircle size={16} />
              Create Assessment
            </Link>
          </div>
        ) : (
          <div className="grid grid-2 stagger">
            {assessments.slice(0, 6).map((a) => (
              <DashAssessmentCard key={a.id} assessment={a} />
            ))}
          </div>
        )}

        {assessments.length > 6 && (
          <div style={{ textAlign: 'center', marginTop: 'var(--space-6)' }}>
            <Link to="/teacher/assessments" className="btn btn-secondary">
              View all assessments
            </Link>
          </div>
        )}
      </main>
    </div>
  );
}

function DashAssessmentCard({ assessment: a }: { assessment: Assessment }) {
  return (
    <Link to={`/teacher/assessments/${a.id}`} style={{ textDecoration: 'none' }}>
      <div className="assessment-card">
        <div className="assessment-card-header">
          <h3 className="assessment-card-title">{a.title}</h3>
          <span className={`badge badge-${a.status.toLowerCase()}`}>{a.status}</span>
        </div>
        <p className="assessment-card-desc">{a.description || 'No description'}</p>
        <div className="assessment-card-meta">
          <span className="assessment-card-meta-item">
            <Clock size={13} /> {a.durationMinutes} min
          </span>
          <span className="assessment-card-meta-item">
            <CheckCircle size={13} /> {a.passingScore}% to pass
          </span>
          {a.questionCount !== undefined && (
            <span className="assessment-card-meta-item">
              <Users size={13} /> {a.questionCount} Q
            </span>
          )}
        </div>
      </div>
    </Link>
  );
}

function getGreeting() {
  const h = new Date().getHours();
  if (h < 12) return 'morning';
  if (h < 18) return 'afternoon';
  return 'evening';
}
