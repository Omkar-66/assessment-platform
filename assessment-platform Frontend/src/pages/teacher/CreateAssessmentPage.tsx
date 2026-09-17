import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { teacherApi } from '../../api/teacher';
import { Navbar } from '../../components/Navbar';
import { AlertCircle, ArrowLeft } from 'lucide-react';

export function CreateAssessmentPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState<{
    title: string;
    description: string;
    durationMinutes: number | '';
    passingScore: number | '';
  }>({
    title: '',
    description: '',
    durationMinutes: 30,
    passingScore: 60,
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await teacherApi.createAssessment({
        title: form.title,
        description: form.description,
        durationMinutes: Number(form.durationMinutes) || 30,
      });
      navigate(`/teacher/assessments/${res.data.id}`);
    } catch (err: unknown) {
      setError(
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        'Failed to create assessment.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <Navbar />
      <main className="page-content" style={{ maxWidth: 640 }}>
        <button className="btn btn-ghost" onClick={() => navigate(-1)} style={{ marginBottom: 'var(--space-6)' }}>
          <ArrowLeft size={16} /> Back
        </button>

        <h1 className="page-title" style={{ marginBottom: 'var(--space-2)' }}>New Assessment</h1>
        <p className="page-subtitle" style={{ marginBottom: 'var(--space-8)' }}>
          Fill in the details. You can add questions after creating.
        </p>

        {error && (
          <div className="alert alert-error" style={{ marginBottom: 'var(--space-5)' }}>
            <AlertCircle size={16} /> {error}
          </div>
        )}

        <div className="card">
          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }}>
            <div className="form-group">
              <label className="form-label" htmlFor="ca-title">Title</label>
              <input
                id="ca-title"
                type="text"
                className="form-input"
                placeholder="e.g. Introduction to Biology"
                value={form.title}
                onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="ca-description">Description</label>
              <textarea
                id="ca-description"
                className="form-input form-textarea"
                placeholder="Brief description of this assessment…"
                value={form.description}
                onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
              />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-5)' }}>
              <div className="form-group">
                <label className="form-label" htmlFor="ca-duration">Duration (minutes)</label>
                <input
                  id="ca-duration"
                  type="number"
                  className="form-input"
                  min={1}
                  max={480}
                  value={form.durationMinutes}
                  onChange={(e) => {
                    const val = e.target.value;
                    setForm((f) => ({
                      ...f,
                      durationMinutes: val === '' ? '' : Math.max(0, parseInt(val, 10) || 0),
                    }));
                  }}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="ca-passing">Passing Score (%)</label>
                <input
                  id="ca-passing"
                  type="number"
                  className="form-input"
                  min={0}
                  max={100}
                  value={form.passingScore}
                  onChange={(e) => {
                    const val = e.target.value;
                    setForm((f) => ({
                      ...f,
                      passingScore: val === '' ? '' : Math.max(0, parseInt(val, 10) || 0),
                    }));
                  }}
                  required
                />
              </div>
            </div>

            <div style={{ display: 'flex', gap: 'var(--space-3)', justifyContent: 'flex-end', marginTop: 'var(--space-2)' }}>
              <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>
                Cancel
              </button>
              <button id="create-assessment-submit" type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'Creating…' : 'Create Assessment'}
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}
