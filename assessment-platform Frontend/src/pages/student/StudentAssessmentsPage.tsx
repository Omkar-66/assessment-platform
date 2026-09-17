import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { studentApi } from '../../api/student';
import { Navbar } from '../../components/Navbar';
import type { Assessment } from '../../types';
import { Clock, CheckCircle, BookOpen, Play } from 'lucide-react';

export function StudentAssessmentsPage() {
  const [assessments, setAssessments] = useState<Assessment[]>([]);
  const [loading, setLoading] = useState(true);
  const [startingId, setStartingId] = useState<number | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    studentApi.getPublishedAssessments()
      .then((r) => setAssessments(r.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const handleStart = async (assessmentId: number) => {
    setStartingId(assessmentId);
    try {
      const res = await studentApi.startAttempt(assessmentId);
      navigate(`/student/attempts/${res.data.attemptId}`);
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      alert(msg || 'Could not start assessment. You may have already attempted it.');
    } finally {
      setStartingId(null);
    }
  };

  return (
    <div className="page">
      <Navbar />
      <main className="page-content">
        <div className="page-header">
          <div>
            <h1 className="page-title">Browse Assessments</h1>
            <p className="page-subtitle">
              {loading ? 'Loading…' : `${assessments.length} published assessment${assessments.length !== 1 ? 's' : ''} available`}
            </p>
          </div>
        </div>

        {loading ? (
          <div className="loading-screen" style={{ minHeight: 300 }}>
            <div className="spinner" />
          </div>
        ) : assessments.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon"><BookOpen size={28} /></div>
            <p className="empty-state-title">No assessments available</p>
            <p className="empty-state-desc">
              Check back later — your teachers will publish assessments here.
            </p>
          </div>
        ) : (
          <div className="grid grid-2 stagger">
            {assessments.map((a) => (
              <div key={a.id} className="assessment-card">
                <div className="assessment-card-header">
                  <h2 className="assessment-card-title">{a.title}</h2>
                  <span className="badge badge-published">Published</span>
                </div>
                <p className="assessment-card-desc">{a.description || 'No description provided.'}</p>
                <div className="assessment-card-meta">
                  <span className="assessment-card-meta-item">
                    <Clock size={13} /> {a.durationMinutes} min
                  </span>
                  <span className="assessment-card-meta-item">
                    <CheckCircle size={13} /> {a.passingScore}% to pass
                  </span>
                </div>
                <div className="assessment-card-actions">
                  <button
                    id={`start-assessment-${a.id}`}
                    className="btn btn-primary btn-sm"
                    disabled={startingId === a.id}
                    onClick={() => handleStart(a.id)}
                  >
                    <Play size={13} />
                    {startingId === a.id ? 'Starting…' : 'Start Assessment'}
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
