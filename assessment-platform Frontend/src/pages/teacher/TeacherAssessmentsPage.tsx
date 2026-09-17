import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { teacherApi } from '../../api/teacher';
import { Navbar } from '../../components/Navbar';
import type { Assessment } from '../../types';
import {
  PlusCircle, Clock, CheckCircle, Trash2,
  Globe, FileText, BookOpen
} from 'lucide-react';

export function TeacherAssessmentsPage() {
  const [assessments, setAssessments] = useState<Assessment[]>([]);
  const [loading, setLoading] = useState(true);
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const [publishingId, setPublishingId] = useState<number | null>(null);
  const navigate = useNavigate();

  const load = () => {
    setLoading(true);
    teacherApi.getMyAssessments()
      .then((r) => setAssessments(r.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this assessment? This cannot be undone.')) return;
    setDeletingId(id);
    try {
      await teacherApi.deleteAssessment(id);
      setAssessments((prev) => prev.filter((a) => a.id !== id));
    } catch (err: unknown) {
      alert((err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Delete failed');
    } finally {
      setDeletingId(null);
    }
  };

  const handlePublish = async (id: number) => {
    if (!confirm('Publish this assessment? Students will be able to take it.')) return;
    setPublishingId(id);
    try {
      const res = await teacherApi.publishAssessment(id);
      setAssessments((prev) => prev.map((a) => (a.id === id ? res.data : a)));
    } catch (err: unknown) {
      alert((err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Publish failed');
    } finally {
      setPublishingId(null);
    }
  };

  return (
    <div className="page">
      <Navbar />
      <main className="page-content">
        <div className="page-header">
          <div>
            <h1 className="page-title">Assessments</h1>
            <p className="page-subtitle">
              {loading ? 'Loading…' : `${assessments.length} assessment${assessments.length !== 1 ? 's' : ''}`}
            </p>
          </div>
          <Link to="/teacher/assessments/new" className="btn btn-primary">
            <PlusCircle size={16} />
            New Assessment
          </Link>
        </div>

        {loading ? (
          <div className="loading-screen" style={{ minHeight: 300 }}>
            <div className="spinner" />
          </div>
        ) : assessments.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon"><BookOpen size={28} /></div>
            <p className="empty-state-title">No assessments yet</p>
            <p className="empty-state-desc">Create your first assessment to get started.</p>
            <Link to="/teacher/assessments/new" className="btn btn-primary">
              <PlusCircle size={16} /> Create Assessment
            </Link>
          </div>
        ) : (
          <div className="grid stagger" style={{ gridTemplateColumns: '1fr' }}>
            {assessments.map((a) => (
              <div key={a.id} className="card" style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-5)' }}>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)', marginBottom: 'var(--space-2)' }}>
                    <h2 style={{ fontSize: 17, fontWeight: 700, color: 'var(--text-primary)' }}>{a.title}</h2>
                    <span className={`badge badge-${a.status.toLowerCase()}`}>{a.status}</span>
                  </div>
                  <p style={{ fontSize: 14, color: 'var(--text-secondary)', marginBottom: 'var(--space-3)' }}>
                    {a.description || 'No description'}
                  </p>
                  <div className="assessment-card-meta">
                    <span className="assessment-card-meta-item"><Clock size={13} /> {a.durationMinutes} min</span>
                    <span className="assessment-card-meta-item"><CheckCircle size={13} /> {a.passingScore}% to pass</span>
                    <span className="assessment-card-meta-item"><FileText size={13} /> {a.questionCount ?? '—'} questions</span>
                  </div>
                </div>

                <div style={{ display: 'flex', gap: 'var(--space-2)', flexShrink: 0, flexWrap: 'wrap', justifyContent: 'flex-end' }}>
                  <button
                    className="btn btn-secondary btn-sm"
                    onClick={() => navigate(`/teacher/assessments/${a.id}`)}
                  >
                    Manage
                  </button>

                  {a.status === 'DRAFT' && (
                    <button
                      className="btn btn-primary btn-sm"
                      disabled={publishingId === a.id}
                      onClick={() => handlePublish(a.id)}
                    >
                      <Globe size={13} />
                      {publishingId === a.id ? 'Publishing…' : 'Publish'}
                    </button>
                  )}

                  <button
                    className="btn btn-danger btn-sm"
                    disabled={deletingId === a.id}
                    onClick={() => handleDelete(a.id)}
                  >
                    <Trash2 size={13} />
                    {deletingId === a.id ? '…' : 'Delete'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
