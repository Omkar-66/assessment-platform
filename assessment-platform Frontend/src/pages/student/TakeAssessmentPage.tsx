import { useEffect, useRef, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { studentApi } from '../../api/student';
import type { AttemptWithQuestionsResponse, Question } from '../../types';
import { Clock, ChevronLeft, ChevronRight, Send, ShieldAlert, Maximize, AlertTriangle } from 'lucide-react';

export function TakeAssessmentPage() {
  const { attemptId } = useParams<{ attemptId: string }>();
  const navigate = useNavigate();
  const aId = Number(attemptId);

  const [data, setData] = useState<AttemptWithQuestionsResponse | null>(null);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [selectedOptions, setSelectedOptions] = useState<Record<number, number>>({});
  const [currentIdx, setCurrentIdx] = useState(0);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [savingQ, setSavingQ] = useState<number | null>(null);

  // Security & Proctoring State
  const [fullscreenStartModalOpen, setFullscreenStartModalOpen] = useState(true);
  const [fullscreenWarningModalOpen, setFullscreenWarningModalOpen] = useState(false);
  const [testStarted, setTestStarted] = useState(false);
  const submittingRef = useRef(false);
  const fullscreenWarningsRef = useRef(0);

  // Timer state
  const [secondsLeft, setSecondsLeft] = useState<number | null>(null);
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  // Restore cached answers from localStorage on mount
  useEffect(() => {
    try {
      const cached = localStorage.getItem(`answers_attempt_${aId}`);
      if (cached) {
        setSelectedOptions(JSON.parse(cached));
      }
    } catch {
      // Ignore parse errors
    }
  }, [aId]);

  // Load attempt questions & status
  useEffect(() => {
    studentApi.getAttemptQuestions(aId)
      .then((r) => {
        setData(r.data);
        setQuestions(r.data.questions);

        // If attempt is already submitted, redirect to review
        if (r.data.status === 'SUBMITTED') {
          navigate(`/student/attempts/${aId}/review`, { replace: true });
          return;
        }

        // Calculate seconds remaining from startedAt + durationMinutes
        const startedAt = new Date(r.data.startedAt).getTime();
        const durationMs = r.data.durationMinutes * 60 * 1000;
        const elapsed = Date.now() - startedAt;
        const remaining = Math.max(0, Math.floor((durationMs - elapsed) / 1000));
        setSecondsLeft(remaining);
      })
      .catch(() => navigate('/student'))
      .finally(() => setLoading(false));
  }, [aId, navigate]);

  // Auto-submit helper
  const handleAutoSubmit = useCallback(async () => {
    if (submittingRef.current) return;
    submittingRef.current = true;
    setSubmitting(true);
    localStorage.removeItem(`answers_attempt_${aId}`);
    try {
      if (document.fullscreenElement) {
        await document.exitFullscreen().catch(() => {});
      }
    } catch {
      // ignore
    }
    try {
      await studentApi.submitAttempt(aId);
    } catch {
      // Ignore submission error if already submitted
    } finally {
      navigate(`/student/attempts/${aId}/review`, { replace: true });
    }
  }, [aId, navigate]);

  // Countdown timer
  useEffect(() => {
    if (secondsLeft === null) return;
    if (secondsLeft <= 0) {
      handleAutoSubmit();
      return;
    }
    timerRef.current = setInterval(() => {
      setSecondsLeft((prev) => {
        if (prev === null || prev <= 1) {
          clearInterval(timerRef.current!);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(timerRef.current!);
  }, [secondsLeft, handleAutoSubmit]);

  // Watch for timer 0
  useEffect(() => {
    if (secondsLeft === 0) handleAutoSubmit();
  }, [secondsLeft, handleAutoSubmit]);

  // ── SECURITY PROCTORING LISTENERS ──────────────────────────────────────────
  useEffect(() => {
    if (!testStarted || submittingRef.current) return;

    const handleViolation = (reason: string) => {
      if (submittingRef.current) return;
      alert(`⚠️ SECURITY VIOLATION\n\n${reason}\nYour assessment is being automatically submitted now.`);
      handleAutoSubmit();
    };

    // 1. Tab switch or window minimized
    const handleVisibilityChange = () => {
      if (document.visibilityState === 'hidden') {
        handleViolation('Tab switch detected.');
      }
    };

    // 3. Exiting Full Screen mode — 1 WARNING POLICY (with WebKit mobile support)
    const handleFullscreenChange = () => {
      const isFs = !!(document.fullscreenElement || (document as any).webkitFullscreenElement);
      if (!isFs && testStarted && !submittingRef.current) {
        if (fullscreenWarningsRef.current === 0) {
          fullscreenWarningsRef.current = 1;
          setFullscreenWarningModalOpen(true);
        } else {
          handleViolation('Exited Full Screen mode a second time.');
        }
      }
    };

    // 4. Prevent Right Click & Copy/Paste
    const preventAction = (e: Event) => e.preventDefault();

    document.addEventListener('visibilitychange', handleVisibilityChange);
    document.addEventListener('fullscreenchange', handleFullscreenChange);
    document.addEventListener('webkitfullscreenchange', handleFullscreenChange);
    document.addEventListener('contextmenu', preventAction);
    document.addEventListener('copy', preventAction);
    document.addEventListener('cut', preventAction);
    document.addEventListener('paste', preventAction);

    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
      document.removeEventListener('fullscreenchange', handleFullscreenChange);
      document.removeEventListener('webkitfullscreenchange', handleFullscreenChange);
      document.removeEventListener('contextmenu', preventAction);
      document.removeEventListener('copy', preventAction);
      document.removeEventListener('cut', preventAction);
      document.removeEventListener('paste', preventAction);
    };
  }, [testStarted, handleAutoSubmit]);

  // Request Fullscreen & Start Test (Mobile Safe)
  const enterFullscreenAndStart = async () => {
    const el = document.documentElement as any;
    try {
      if (el.requestFullscreen) {
        await el.requestFullscreen();
      } else if (el.webkitRequestFullscreen) {
        await el.webkitRequestFullscreen();
      }
    } catch {
      // Proceed even if browser or iOS restrictions prevent fullscreen
    }
    setFullscreenStartModalOpen(false);
    setTestStarted(true);
  };

  // Re-enter Fullscreen after 1st Warning
  const reenterFullscreen = async () => {
    const el = document.documentElement as any;
    try {
      if (el.requestFullscreen) {
        await el.requestFullscreen();
      } else if (el.webkitRequestFullscreen) {
        await el.webkitRequestFullscreen();
      }
    } catch {
      // ignore
    }
    setFullscreenWarningModalOpen(false);
  };

  // Save answer on selection
  const handleSelectOption = async (questionId: number, optionId: number) => {
    if (submitting) return;
    const updated = { ...selectedOptions, [questionId]: optionId };
    setSelectedOptions(updated);
    try {
      localStorage.setItem(`answers_attempt_${aId}`, JSON.stringify(updated));
    } catch {
      // ignore localstorage quota error
    }

    setSavingQ(questionId);
    try {
      await studentApi.saveAnswer(aId, questionId, { selectedOptionId: optionId });
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      if (msg?.includes('submitted')) {
        navigate(`/student/attempts/${aId}/review`, { replace: true });
      }
    } finally {
      setSavingQ(null);
    }
  };

  const handleSubmit = async () => {
    if (!confirm('Submit this assessment? You cannot make changes after submitting.')) return;
    submittingRef.current = true;
    setSubmitting(true);
    clearInterval(timerRef.current!);
    localStorage.removeItem(`answers_attempt_${aId}`);
    try {
      if (document.fullscreenElement) {
        await document.exitFullscreen().catch(() => {});
      }
    } catch {
      // ignore
    }
    try {
      await studentApi.submitAttempt(aId);
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      alert(msg || 'Submission failed');
    } finally {
      navigate(`/student/attempts/${aId}/review`, { replace: true });
    }
  };

  // Timer display
  const formatTime = (s: number) => {
    const m = Math.floor(s / 60);
    const sec = s % 60;
    return `${m}:${String(sec).padStart(2, '0')}`;
  };

  const timerClass =
    secondsLeft !== null && secondsLeft <= 60
      ? 'quiz-timer critical'
      : secondsLeft !== null && secondsLeft <= 300
      ? 'quiz-timer warning'
      : 'quiz-timer';

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="spinner" />
      </div>
    );
  }

  if (!data || questions.length === 0) {
    return (
      <div className="loading-screen">
        <p style={{ color: 'var(--text-secondary)' }}>No questions found.</p>
      </div>
    );
  }

  const currentQ = questions[currentIdx];
  const answered = Object.keys(selectedOptions).length;
  const progressPct = Math.round((answered / questions.length) * 100);

  return (
    <div className="quiz-page" style={{ userSelect: 'none' }}>
      {/* 1. INITIAL FULLSCREEN START MODAL OVERLAY */}
      {fullscreenStartModalOpen && (
        <div style={{
          position: 'fixed', inset: 0, zIndex: 99999,
          background: 'rgba(10, 10, 15, 0.96)',
          backdropFilter: 'blur(12px)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          padding: 'var(--space-6)',
        }}>
          <div className="card" style={{ maxWidth: 520, textAlign: 'center', padding: 'var(--space-8)' }}>
            <div style={{
              width: 56, height: 56, borderRadius: '50%',
              background: 'var(--color-accent-dim)',
              color: 'var(--color-accent)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              margin: '0 auto var(--space-4)'
            }}>
              <ShieldAlert size={28} />
            </div>

            <h2 style={{ fontSize: 22, fontWeight: 800, marginBottom: 'var(--space-2)' }}>
              Proctored Security Mode
            </h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: 14, marginBottom: 'var(--space-6)', lineHeight: 1.6 }}>
              This assessment is proctored for fairness and integrity.
              <br /><strong style={{ color: 'var(--color-warning)' }}>Strict Rules:</strong>
            </p>

            <ul style={{
              textAlign: 'left', fontSize: 13, color: 'var(--text-secondary)',
              marginBottom: 'var(--space-6)', display: 'flex', flexDirection: 'column', gap: 8,
              paddingLeft: 'var(--space-4)'
            }}>
              <li>Must be taken in <strong>Full Screen Mode</strong>. (1 warning allowed before auto-submit)</li>
              <li>Changing tabs or window focus will <strong>automatically submit</strong> your test immediately.</li>
              <li>Right-click, copy, and paste are disabled.</li>
            </ul>

            <button
              className="btn btn-primary btn-full btn-lg"
              onClick={enterFullscreenAndStart}
              style={{ justifyContent: 'center' }}
            >
              <Maximize size={18} /> Enter Full Screen &amp; Start Test
            </button>
          </div>
        </div>
      )}

      {/* 2. FULLSCREEN WARNING MODAL OVERLAY (FIRST OFFENSE) */}
      {fullscreenWarningModalOpen && !submitting && (
        <div style={{
          position: 'fixed', inset: 0, zIndex: 999999,
          background: 'rgba(180, 20, 20, 0.95)',
          backdropFilter: 'blur(12px)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          padding: 'var(--space-6)',
        }}>
          <div className="card" style={{ maxWidth: 520, textAlign: 'center', padding: 'var(--space-8)', background: '#18181b', border: '2px solid var(--color-error)' }}>
            <div style={{
              width: 56, height: 56, borderRadius: '50%',
              background: 'var(--color-error-dim)',
              color: 'var(--color-error)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              margin: '0 auto var(--space-4)'
            }}>
              <AlertTriangle size={28} />
            </div>

            <h2 style={{ fontSize: 22, fontWeight: 800, marginBottom: 'var(--space-2)', color: 'var(--color-error)' }}>
              ⚠️ Full Screen Warning (1 / 1)
            </h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: 14, marginBottom: 'var(--space-6)', lineHeight: 1.6 }}>
              You have exited Full Screen mode!
              <br />
              <strong style={{ color: '#fff' }}>Exiting Full Screen mode one more time will result in IMMEDIATE AUTOMATIC SUBMISSION.</strong>
            </p>

            <button
              className="btn btn-danger btn-full btn-lg"
              onClick={reenterFullscreen}
              style={{ justifyContent: 'center', fontWeight: 800 }}
            >
              <Maximize size={18} /> Re-enter Full Screen Mode Now
            </button>
          </div>
        </div>
      )}

      {/* Quiz Header */}
      <div className="quiz-header">
        <div>
          <div className="quiz-title">{data.assessmentTitle}</div>
          <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>
            {answered}/{questions.length} answered
          </div>
        </div>

        <div className={timerClass}>
          <Clock size={16} />
          {secondsLeft !== null ? formatTime(secondsLeft) : '--:--'}
        </div>
      </div>

      {/* Progress */}
      <div className="quiz-progress-row">
        <div className="progress-bar-track">
          <div
            className="progress-bar-fill"
            style={{ width: `${progressPct}%` }}
          />
        </div>
      </div>

      {/* Body */}
      <div className="quiz-body">
        <div className="question-card">
          <div className="question-number">Question {currentIdx + 1} of {questions.length}</div>
          <div className="question-text">{currentQ.questionText}</div>
          <div className="question-marks">{currentQ.marks} mark{currentQ.marks !== 1 ? 's' : ''}</div>

          <div className="options-list">
            {currentQ.options.map((opt) => {
              const isSelected = selectedOptions[currentQ.id] === opt.id;
              return (
                <button
                  key={opt.id}
                  className={`option-item ${isSelected ? 'selected' : ''}`}
                  onClick={() => handleSelectOption(currentQ.id, opt.id)}
                  disabled={submitting || savingQ === currentQ.id}
                  aria-pressed={isSelected}
                >
                  <div className="option-radio">
                    {isSelected && <div className="option-radio-dot" />}
                  </div>
                  <span className="option-text">{opt.optionText}</span>
                  {savingQ === currentQ.id && isSelected && (
                    <span style={{ fontSize: 12, color: 'var(--text-muted)', marginLeft: 'auto' }}>Saving…</span>
                  )}
                </button>
              );
            })}
          </div>
        </div>

        {/* Navigation */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 'var(--space-4)' }}>
          <button
            className="btn btn-secondary"
            disabled={currentIdx === 0}
            onClick={() => setCurrentIdx((i) => i - 1)}
          >
            <ChevronLeft size={16} /> Previous
          </button>

          {/* Question dots */}
          <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', justifyContent: 'center', flex: 1 }}>
            {questions.map((q, idx) => (
              <button
                key={q.id}
                onClick={() => setCurrentIdx(idx)}
                style={{
                  width: 30,
                  height: 30,
                  borderRadius: '50%',
                  border: idx === currentIdx ? '2px solid var(--color-accent)' : '2px solid var(--color-border)',
                  background: selectedOptions[q.id]
                    ? 'var(--color-accent)'
                    : idx === currentIdx
                    ? 'var(--color-accent-dim)'
                    : 'var(--color-surface-2)',
                  color: selectedOptions[q.id] || idx === currentIdx ? '#fff' : 'var(--text-muted)',
                  fontSize: 12,
                  fontWeight: 700,
                  cursor: 'pointer',
                  transition: 'all 0.15s',
                }}
              >
                {idx + 1}
              </button>
            ))}
          </div>

          {currentIdx < questions.length - 1 ? (
            <button
              className="btn btn-secondary"
              onClick={() => setCurrentIdx((i) => i + 1)}
            >
              Next <ChevronRight size={16} />
            </button>
          ) : (
            <button
              id="submit-attempt-btn"
              className="btn btn-primary"
              onClick={handleSubmit}
              disabled={submitting}
            >
              <Send size={15} />
              {submitting ? 'Submitting…' : 'Submit'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
