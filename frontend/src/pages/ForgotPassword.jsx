import { useState } from 'react'
import { Link } from 'react-router-dom'
import api, { errorMessage } from '../services/api'
import { SECURITY_QUESTIONS } from '../constants/securityQuestions'

const initialPasswords = { newPassword: '', confirmPassword: '' }
const initialQuestionReset = {
  recoveryCode: '',
  securityQuestion: SECURITY_QUESTIONS[0],
  securityAnswer: '',
  confirmSecurityAnswer: ''
}

export default function ForgotPassword() {
  const [step, setStep] = useState(1)
  const [email, setEmail] = useState('')
  const [question, setQuestion] = useState('')
  const [securityAnswer, setSecurityAnswer] = useState('')
  const [challengeToken, setChallengeToken] = useState('')
  const [recoveryCode, setRecoveryCode] = useState('')
  const [resetToken, setResetToken] = useState('')
  const [passwords, setPasswords] = useState(initialPasswords)
  const [questionReset, setQuestionReset] = useState(initialQuestionReset)
  const [newRecoveryCode, setNewRecoveryCode] = useState('')
  const [copied, setCopied] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const clearAlerts = () => { setError(''); setMessage('') }

  const fetchQuestion = async event => {
    event.preventDefault()
    setBusy(true); clearAlerts()
    try {
      const { data } = await api.post('/auth/recovery-question', { email })
      setQuestion(data.question)
      setSecurityAnswer('')
      setStep(2)
    } catch (err) {
      setError(errorMessage(err))
    } finally { setBusy(false) }
  }

  const verifyAnswer = async event => {
    event.preventDefault()
    setBusy(true); clearAlerts()
    try {
      const { data } = await api.post('/auth/verify-security-answer', { email, securityAnswer })
      setChallengeToken(data.challengeToken)
      setMessage(data.message)
      setRecoveryCode('')
      setStep(3)
    } catch (err) {
      setError(errorMessage(err))
    } finally { setBusy(false) }
  }

  const verifyCode = async event => {
    event.preventDefault()
    setBusy(true); clearAlerts()
    try {
      const { data } = await api.post('/auth/verify-recovery-code', { email, challengeToken, recoveryCode })
      setResetToken(data.resetToken)
      setMessage(data.message)
      setStep(4)
    } catch (err) {
      setError(errorMessage(err))
    } finally { setBusy(false) }
  }

  const replaceQuestion = async event => {
    event.preventDefault()
    clearAlerts()
    if (questionReset.securityAnswer.trim().toLowerCase() !== questionReset.confirmSecurityAnswer.trim().toLowerCase()) {
      setError('Security answer and confirmation do not match')
      return
    }

    setBusy(true)
    try {
      const { confirmSecurityAnswer, ...payload } = questionReset
      const { data } = await api.post('/auth/reset-security-question', { email, ...payload })
      setResetToken(data.resetToken)
      setNewRecoveryCode(data.recoveryCode)
      setMessage(data.message)
      setStep(7)
    } catch (err) {
      setError(errorMessage(err))
    } finally { setBusy(false) }
  }

  const resetPassword = async event => {
    event.preventDefault()
    clearAlerts()
    if (passwords.newPassword !== passwords.confirmPassword) {
      setError('New password and confirmation do not match')
      return
    }

    setBusy(true)
    try {
      const { data } = await api.post('/auth/reset-password', {
        resetToken,
        newPassword: passwords.newPassword,
        confirmPassword: passwords.confirmPassword
      })
      setMessage(data.message)
      setStep(5)
      setPasswords(initialPasswords)
    } catch (err) {
      setError(errorMessage(err))
    } finally { setBusy(false) }
  }

  const copyNewCode = async () => {
    await navigator.clipboard.writeText(newRecoveryCode)
    setCopied(true)
    setTimeout(() => setCopied(false), 1800)
  }

  const title = {
    1: 'Forgot password?',
    2: 'Answer your question',
    3: 'Enter recovery code',
    4: 'Create new password',
    5: 'Password updated',
    6: 'Reset security question',
    7: 'Save your new code'
  }[step]

  return <div className="auth-page">
    <div className="auth-card reset-card">
      <div className="eyebrow">Email-free account recovery</div>
      <h1>{title}</h1>
      <p className="text-secondary">
        {step === 1 && 'Enter the email address used for your QuizSphere account.'}
        {step === 2 && 'Enter the answer you selected while creating your account.'}
        {step === 3 && 'Enter the recovery code that was shown after registration.'}
        {step === 4 && 'Choose a secure new password for your account.'}
        {step === 5 && 'Your password has been reset successfully.'}
        {step === 6 && 'Use your recovery code to replace the forgotten question and answer.'}
        {step === 7 && 'Your old recovery code is now invalid. Save this replacement code.'}
      </p>

      {step <= 4 && <div className="reset-steps" aria-label="Password reset progress">
        {[1, 2, 3, 4].map(number => <span key={number} className={step >= number ? 'active' : ''}>{number}</span>)}
      </div>}

      {error && <div className="alert alert-danger">{error}</div>}
      {message && ![5, 7].includes(step) && <div className="alert alert-success">{message}</div>}

      {step === 1 && <form onSubmit={fetchQuestion}>
        <label className="form-label">Registered email address</label>
        <input className="form-control form-control-lg mb-4" type="email" autoComplete="email" required
          value={email} onChange={event => setEmail(event.target.value)} placeholder="you@example.com" />
        <button className="btn btn-primary btn-lg w-100" disabled={busy}>{busy ? 'Checking...' : 'Continue'}</button>
      </form>}

      {step === 2 && <form onSubmit={verifyAnswer}>
        <div className="security-question-card mb-3"><i className="bi bi-patch-question" /><strong>{question}</strong></div>
        <label className="form-label">Your security answer</label>
        <input className="form-control form-control-lg mb-3" type="password" autoComplete="off" minLength="2" maxLength="100" required
          value={securityAnswer} onChange={event => setSecurityAnswer(event.target.value)} />
        <button className="btn btn-primary btn-lg w-100" disabled={busy}>{busy ? 'Verifying...' : 'Verify answer'}</button>
        <div className="reset-actions">
          <button type="button" className="btn btn-link p-0" onClick={() => { setStep(6); clearAlerts() }}>I forgot the answer</button>
          <button type="button" className="btn btn-link p-0" onClick={() => { setStep(1); clearAlerts() }}>Change email</button>
        </div>
      </form>}

      {step === 3 && <form onSubmit={verifyCode}>
        <label className="form-label">Recovery code</label>
        <input className="form-control form-control-lg recovery-code-input mb-3" type="text" autoComplete="off" maxLength="20" required
          value={recoveryCode} onChange={event => setRecoveryCode(event.target.value.toUpperCase())} placeholder="QSR-XXXX-XXXX-XXXX" />
        <button className="btn btn-primary btn-lg w-100" disabled={busy}>{busy ? 'Verifying...' : 'Verify recovery code'}</button>
        <div className="reset-actions">
          <button type="button" className="btn btn-link p-0" onClick={() => { setStep(2); clearAlerts() }}>Back to question</button>
        </div>
      </form>}

      {step === 4 && <form onSubmit={resetPassword}>
        <label className="form-label">New password</label>
        <input className="form-control form-control-lg mb-3" type="password" autoComplete="new-password" minLength="6" required
          value={passwords.newPassword} onChange={event => setPasswords({ ...passwords, newPassword: event.target.value })} />
        <label className="form-label">Confirm new password</label>
        <input className="form-control form-control-lg mb-4" type="password" autoComplete="new-password" minLength="6" required
          value={passwords.confirmPassword} onChange={event => setPasswords({ ...passwords, confirmPassword: event.target.value })} />
        <div className="password-hint">Use at least 6 characters. A longer unique password is recommended.</div>
        <button className="btn btn-primary btn-lg w-100 mt-3" disabled={busy}>{busy ? 'Updating password...' : 'Reset password'}</button>
      </form>}

      {step === 5 && <div className="reset-success text-center">
        <div className="success-icon"><i className="bi bi-check-lg" /></div>
        <p>{message}</p>
        <Link className="btn btn-primary btn-lg w-100" to="/login">Sign in with new password</Link>
      </div>}

      {step === 6 && <form onSubmit={replaceQuestion}>
        <div className="alert alert-warning small">This option works only with your existing recovery code. Without that code, the question cannot be replaced.</div>
        <label className="form-label">Current recovery code</label>
        <input className="form-control mb-3 recovery-code-input" required maxLength="20" value={questionReset.recoveryCode}
          onChange={event => setQuestionReset({ ...questionReset, recoveryCode: event.target.value.toUpperCase() })} placeholder="QSR-XXXX-XXXX-XXXX" />
        <label className="form-label">New security question</label>
        <select className="form-select mb-3" value={questionReset.securityQuestion}
          onChange={event => setQuestionReset({ ...questionReset, securityQuestion: event.target.value })}>
          {SECURITY_QUESTIONS.map(item => <option key={item}>{item}</option>)}
        </select>
        <label className="form-label">New security answer</label>
        <input className="form-control mb-3" type="password" minLength="2" maxLength="100" required value={questionReset.securityAnswer}
          onChange={event => setQuestionReset({ ...questionReset, securityAnswer: event.target.value })} />
        <label className="form-label">Confirm new security answer</label>
        <input className="form-control mb-4" type="password" minLength="2" maxLength="100" required value={questionReset.confirmSecurityAnswer}
          onChange={event => setQuestionReset({ ...questionReset, confirmSecurityAnswer: event.target.value })} />
        <button className="btn btn-primary btn-lg w-100" disabled={busy}>{busy ? 'Updating...' : 'Replace question and continue'}</button>
        <button type="button" className="btn btn-link w-100 mt-2" onClick={() => { setStep(2); clearAlerts() }}>Back to security question</button>
      </form>}

      {step === 7 && <div className="text-center">
        <div className="success-icon"><i className="bi bi-key" /></div>
        <p className="text-secondary">{message}</p>
        <div className="recovery-code-box">{newRecoveryCode}</div>
        <button type="button" className="btn btn-outline-primary w-100 mb-3" onClick={copyNewCode}>
          <i className={`bi ${copied ? 'bi-check-lg' : 'bi-copy'} me-2`} />{copied ? 'Copied' : 'Copy new recovery code'}
        </button>
        <button type="button" className="btn btn-primary btn-lg w-100" onClick={() => { setStep(4); clearAlerts() }}>
          I saved it — create new password
        </button>
      </div>}

      {step !== 5 && <p className="text-center mt-4 mb-0"><Link to="/login"><i className="bi bi-arrow-left me-2" />Back to sign in</Link></p>}
    </div>
  </div>
}
