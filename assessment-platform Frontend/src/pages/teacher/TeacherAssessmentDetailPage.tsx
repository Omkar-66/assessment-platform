import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { teacherApi } from '../../api/teacher';
import { Navbar } from '../../components/Navbar';
import type { Assessment, Question, TeacherAttemptSummary } from '../../types';
import {
  ArrowLeft, Globe, Trash2, Plus, X, CheckCircle2, Users, Clock
} from 'lucide-react';

export function TeacherAssessmentDetailPage() {
  const { id } = useParams<{ id: string }>();
  const assessmentId = Number(id);
  const navigate = useNavigate();

  const [assessment, setAssessment] = useState<Assessment | null>(null);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [attempts, setAttempts] = useState<TeacherAttemptSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [publishingId, setPublishingId] = useState(false);
  const [showAddQuestion, setShowAddQuestion] = useState(false);
  const [activeTab, setActiveTab] = useState<'questions' | 'results'>('questions');

  const loadAll = async () => {
    setLoading(true);
    try {
      const [assList, qs, atts] = await Promise.all([
        teacherApi.getMyAssessments(),
        teacherApi.getQuestions(assessmentId),
        teacherApi.getAttempts(assessmentId),
      ]);
      const found = assList.data.find((a) => a.id === assessmentId) ?? null;
      setAssessment(found);
      setQuestions(qs.data);
      setAttempts(atts.data);
    } catch {
      navigate('/teacher/assessments');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadAll(); }, [assessmentId]);

  const handlePublish = async () => {
    if (!confirm('Publish this assessment?')) return;
    setPublishingId(true);
    try {
      const res = await teacherApi.publishAssessment(assessmentId);
      setAssessment(res.data);
    } catch (err: unknown) {
      alert((err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed');
    } finally {
      setPublishingId(false);
    }
  };

  const handleDeleteQuestion = async (qId: number) => {
    if (!confirm('Remove this question?')) return;
    try {
      await teacherApi.deleteQuestion(qId);  // FIX: only questionId, no assessmentId
      setQuestions((prev) => prev.filter((q) => q.id !== qId));
    } catch (err: unknown) {
      alert((err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed');
    }
  };

  if (loading) {
    return (
      <div className="page">
        <Navbar />
        <div className="loading-screen"><div className="spinner" /></div>
      </div>
    );
  }

  if (!assessment) return null;

  return (
    <div className="page">
      <Navbar />
      <main className="page-content">
        {/* Back */}
        <button className="btn btn-ghost" onClick={() => navigate('/teacher/assessments')} style={{ marginBottom: 'var(--space-6)' }}>
          <ArrowLeft size={16} /> All Assessments
        </button>

        {/* Header */}
        <div className="page-header" style={{ alignItems: 'flex-start' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)', marginBottom: 'var(--space-2)' }}>
              <h1 className="page-title" style={{ fontSize: 24 }}>{assessment.title}</h1>
              <span className={`badge badge-${assessment.status.toLowerCase()}`}>{assessment.status}</span>
            </div>
            <div className="assessment-card-meta">
              <span className="assessment-card-meta-item"><Clock size={13} /> {assessment.durationMinutes} min</span>
              <span className="assessment-card-meta-item"><CheckCircle2 size={13} /> {assessment.totalMarks} total marks</span>
              <span className="assessment-card-meta-item"><Users size={13} /> {attempts.length} attempt(s)</span>
            </div>
          </div>

          {assessment.status === 'DRAFT' && (
            <button className="btn btn-primary" onClick={handlePublish} disabled={publishingId}>
              <Globe size={15} />
              {publishingId ? 'Publishing…' : 'Publish'}
            </button>
          )}
        </div>

        {/* Tabs */}
        <div style={{ display: 'flex', gap: 'var(--space-2)', marginBottom: 'var(--space-6)', borderBottom: '1px solid var(--color-border)', paddingBottom: 'var(--space-3)' }}>
          <TabBtn active={activeTab === 'questions'} onClick={() => setActiveTab('questions')}>
            Questions ({questions.length})
          </TabBtn>
          <TabBtn active={activeTab === 'results'} onClick={() => setActiveTab('results')}>
            Results ({attempts.length})
          </TabBtn>
        </div>

        {/* Questions Tab */}
        {activeTab === 'questions' && (
          <div>
            {assessment.status === 'DRAFT' && (
              <div style={{ marginBottom: 'var(--space-5)' }}>
                {showAddQuestion ? (
                  <AddQuestionForm
                    assessmentId={assessmentId}
                    nextOrder={questions.length + 1}
                    onAdded={(q) => { setQuestions((prev) => [...prev, q]); setShowAddQuestion(false); }}
                    onCancel={() => setShowAddQuestion(false)}
                  />
                ) : (
                  <button className="btn btn-primary" onClick={() => setShowAddQuestion(true)}>
                    <Plus size={16} /> Add Question
                  </button>
                )}
              </div>
            )}

            {questions.length === 0 ? (
              <div className="empty-state">
                <p className="empty-state-title">No questions yet</p>
                <p className="empty-state-desc">Add questions to this assessment.</p>
              </div>
            ) : (
              <div className="grid stagger" style={{ gridTemplateColumns: '1fr' }}>
                {questions.map((q, idx) => (
                  <div key={q.id} className="card">
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 'var(--space-4)' }}>
                      <div style={{ flex: 1 }}>
                        <p style={{ fontSize: 13, color: 'var(--color-accent)', fontWeight: 600, marginBottom: 'var(--space-2)' }}>
                          Q{idx + 1} · {q.marks} mark{q.marks !== 1 ? 's' : ''}
                        </p>
                        <p style={{ fontSize: 16, fontWeight: 600, marginBottom: 'var(--space-4)' }}>{q.questionText}</p>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
                          {q.options.map((opt) => (
                            <div
                              key={opt.id}
                              style={{
                                display: 'flex', alignItems: 'center', gap: 'var(--space-3)',
                                padding: '8px 14px', borderRadius: 'var(--radius-md)',
                                background: opt.isCorrect ? 'var(--color-success-dim)' : 'var(--color-surface-2)',
                                border: `1px solid ${opt.isCorrect ? 'rgba(34,197,94,0.25)' : 'var(--color-border)'}`,
                                fontSize: 14
                              }}
                            >
                              {opt.isCorrect && <CheckCircle2 size={14} color="var(--color-success)" />}
                              <span style={{ color: opt.isCorrect ? 'var(--color-success)' : 'var(--text-secondary)' }}>
                                {opt.optionText}
                              </span>
                            </div>
                          ))}
                        </div>
                      </div>
                      {assessment.status === 'DRAFT' && (
                        <button className="btn btn-danger btn-sm" onClick={() => handleDeleteQuestion(q.id)}>
                          <Trash2 size={13} />
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Results Tab */}
        {activeTab === 'results' && (
          <div>
            {attempts.length === 0 ? (
              <div className="empty-state">
                <p className="empty-state-title">No attempts yet</p>
                <p className="empty-state-desc">Students haven't taken this assessment yet.</p>
              </div>
            ) : (
              <div className="table-wrapper">
                <table>
                  <thead>
                    <tr>
                      <th>Student</th>
                      <th>Status</th>
                      <th>Score</th>
                      <th>%</th>
                      <th>Passed</th>
                      <th>Started</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    {attempts.map((att) => (
                      <tr key={att.id}>
                        <td style={{ color: 'var(--text-primary)', fontWeight: 500 }}>{att.studentUsername}</td>
                        <td>
                          <span className={`badge badge-${att.status === 'SUBMITTED' ? 'submitted' : 'in-progress'}`}>
                            {att.status}
                          </span>
                        </td>
                        <td>{att.score != null ? `${att.score}/${att.totalMarks}` : '—'}</td>
                        <td>{att.percentage != null ? `${Number(att.percentage).toFixed(1)}%` : '—'}</td>
                        <td>
                          {att.percentage != null ? (
                            <span className={`badge badge-${Number(att.percentage) >= 60 ? 'passed' : 'failed'}`}>
                              {Number(att.percentage) >= 60 ? 'Pass' : 'Fail'}
                            </span>
                          ) : '—'}
                        </td>
                        <td>{new Date(att.startedAt).toLocaleDateString()}</td>
                        <td>
                          {att.status === 'SUBMITTED' && (
                            <button
                              className="btn btn-ghost btn-sm"
                              onClick={() => navigate(`/teacher/assessments/${assessmentId}/attempts/${att.id}`)}
                            >
                              Review
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  );
}

// ─── Tab Button ───────────────────────────────────────────────────────────────
function TabBtn({ active, onClick, children }: { active: boolean; onClick: () => void; children: React.ReactNode }) {
  return (
    <button
      onClick={onClick}
      style={{
        padding: '8px 18px',
        borderRadius: 'var(--radius-md)',
        border: 'none',
        background: active ? 'var(--color-accent-dim)' : 'transparent',
        color: active ? 'var(--color-accent)' : 'var(--text-secondary)',
        fontWeight: 600,
        fontSize: 14,
        cursor: 'pointer',
        transition: 'all 0.2s',
      }}
    >
      {children}
    </button>
  );
}

// ─── Add Question Form ────────────────────────────────────────────────────────
interface AddQuestionFormProps {
  assessmentId: number;
  nextOrder: number;
  onAdded: (q: import('../../types').Question) => void;
  onCancel: () => void;
}

function AddQuestionForm({ assessmentId, nextOrder, onAdded, onCancel }: AddQuestionFormProps) {
  const [questionText, setQuestionText] = useState('');
  const [marks, setMarks] = useState<number | ''>(1);
  const [options, setOptions] = useState([
    { optionText: '', isCorrect: false },
    { optionText: '', isCorrect: false },
    { optionText: '', isCorrect: false },
    { optionText: '', isCorrect: false },
  ]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const updateOption = (idx: number, field: 'optionText' | 'isCorrect', value: string | boolean) => {
    setOptions((prev) =>
      prev.map((o, i) => {
        if (i === idx) return { ...o, [field]: value };
        if (field === 'isCorrect' && value === true) return { ...o, isCorrect: false };
        return o;
      })
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    const validOptions = options.filter((o) => o.optionText.trim());
    if (validOptions.length < 2) { setError('Please provide at least 2 options.'); return; }
    if (!validOptions.some((o) => o.isCorrect)) { setError('Please mark one option as correct.'); return; }

    setLoading(true);
    try {
      // 1. Create Question
      const qRes = await teacherApi.addQuestion(assessmentId, {
        questionText,
        marks: Number(marks) || 1,
        questionOrder: nextOrder,
      });
      const createdQuestion = qRes.data;

      // 2. Create Options sequentially for this question
      const savedOptions = [];
      for (let i = 0; i < validOptions.length; i++) {
        const opt = validOptions[i];
        const optRes = await teacherApi.addOption(createdQuestion.id, {
          optionText: opt.optionText,
          optionOrder: i + 1,
          isCorrect: opt.isCorrect,
        });
        savedOptions.push(optRes.data);
      }

      onAdded({
        ...createdQuestion,
        options: savedOptions,
      });
    } catch (err: unknown) {
      setError((err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed to add question');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card" style={{ marginBottom: 'var(--space-5)' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-5)' }}>
        <h3 style={{ fontWeight: 700, fontSize: 16 }}>Add Question</h3>
        <button className="btn btn-ghost btn-sm" onClick={onCancel}><X size={16} /></button>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 'var(--space-4)' }}>{error}</div>}

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
        <div className="form-group">
          <label className="form-label">Question Text</label>
          <textarea
            className="form-input form-textarea"
            placeholder="Enter the question…"
            value={questionText}
            onChange={(e) => setQuestionText(e.target.value)}
            required
          />
        </div>

        <div className="form-group" style={{ maxWidth: 160 }}>
          <label className="form-label">Marks</label>
          <input
            type="number" min={1} max={100}
            className="form-input"
            value={marks}
            onChange={(e) => {
              const val = e.target.value;
              setMarks(val === '' ? '' : Math.max(1, parseInt(val, 10) || 1));
            }}
          />
        </div>

        <div className="form-group">
          <label className="form-label">Options (select correct)</label>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
            {options.map((opt, idx) => (
              <div key={idx} style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                <input
                  type="radio"
                  name="correct-option"
                  checked={opt.isCorrect}
                  onChange={() => updateOption(idx, 'isCorrect', true)}
                  style={{ accentColor: 'var(--color-accent)', width: 18, height: 18, flexShrink: 0 }}
                />
                <input
                  type="text"
                  className="form-input"
                  placeholder={`Option ${idx + 1}`}
                  value={opt.optionText}
                  onChange={(e) => updateOption(idx, 'optionText', e.target.value)}
                />
              </div>
            ))}
          </div>
        </div>

        <div style={{ display: 'flex', gap: 'var(--space-3)', justifyContent: 'flex-end' }}>
          <button type="button" className="btn btn-secondary" onClick={onCancel}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Adding…' : 'Add Question'}
          </button>
        </div>
      </form>
    </div>
  );
}
