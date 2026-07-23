import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { errorMessage } from '../services/api'
import GoogleAuthButton from '../components/GoogleAuthButton'
import { SECURITY_QUESTIONS } from '../constants/securityQuestions'

const initialForm = {
  fullName: '',
  email: '',
  password: '',
  securityQuestion: SECURITY_QUESTIONS[0],
  securityAnswer: '',
  confirmSecurityAnswer: ''
}

export default function Register() {
  const [form, setForm] = useState(initialForm)
  const [recoveryCode, setRecoveryCode] = useState('')
  const [copied, setCopied] = useState(false)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const { register } = useAuth()
  const navigate = useNavigate()

  const submit = async event => {
    event.preventDefault()
    setError('')
    if (form.securityAnswer.trim().toLowerCase() !== form.confirmSecurityAnswer.trim().toLowerCase()) {
      setError('Security answer and confirmation do not match')
      return
    }

    setBusy(true)
    try {
      const { confirmSecurityAnswer, ...payload } = form
      const data = await register(payload)
      setRecoveryCode(data.recoveryCode)
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  const copyCode = async () => {
    await navigator.clipboard.writeText(recoveryCode)
    setCopied(true)
    setTimeout(() => setCopied(false), 1800)
  }

  if (recoveryCode) {
    return <div className="auth-page"><div className="auth-card reset-card text-center">
      <div className="success-icon"><i className="bi bi-shield-check" /></div>
      <div className="eyebrow">Account created</div>
      <h1>Save your recovery code</h1>
      <p className="text-secondary">This code is required along with your security answer when you reset your password. It will not be shown again.</p>
      <div className="recovery-code-box" aria-label="Recovery code">{recoveryCode}</div>
      <button type="button" className="btn btn-outline-primary w-100 mb-3" onClick={copyCode}>
        <i className={`bi ${copied ? 'bi-check-lg' : 'bi-copy'} me-2`} />{copied ? 'Copied' : 'Copy recovery code'}
      </button>
      <div className="alert alert-warning text-start small">
        Store it in a password manager, a private note, or write it down. Do not share it publicly.
      </div>
      <button className="btn btn-primary btn-lg w-100" onClick={() => navigate('/dashboard')}>
        I saved the code — continue
      </button>
    </div></div>
  }

  return <div className="auth-page"><div className="auth-card reset-card">
    <div className="eyebrow">Join QuizSphere</div><h1>Create account</h1><p className="text-secondary">Track scores and keep an email-free account recovery method.</p>
    {error && <div className="alert alert-danger">{error}</div>}
    <GoogleAuthButton setError={setError} label="signup_with" />
    <div className="auth-divider"><span>or create with email</span></div>
    <form onSubmit={submit}>
      <label className="form-label">Full name</label>
      <input className="form-control form-control-lg mb-3" minLength="2" required value={form.fullName} onChange={e => setForm({ ...form, fullName: e.target.value })} />

      <label className="form-label">Email address</label>
      <input className="form-control form-control-lg mb-3" type="email" autoComplete="email" required value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} />

      <label className="form-label">Password</label>
      <input className="form-control form-control-lg mb-3" type="password" autoComplete="new-password" minLength="6" required value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} />

      <div className="recovery-section mb-3">
        <div className="d-flex gap-2 align-items-start mb-3">
          <i className="bi bi-shield-lock fs-4 text-primary" />
          <div><strong>Account recovery</strong><div className="small text-secondary">Choose an answer you can remember. A separate recovery code will be generated after registration.</div></div>
        </div>
        <label className="form-label">Security question</label>
        <select className="form-select mb-3" value={form.securityQuestion} onChange={e => setForm({ ...form, securityQuestion: e.target.value })}>
          {SECURITY_QUESTIONS.map(question => <option key={question}>{question}</option>)}
        </select>
        <label className="form-label">Security answer</label>
        <input className="form-control mb-3" type="password" autoComplete="off" minLength="2" maxLength="100" required value={form.securityAnswer} onChange={e => setForm({ ...form, securityAnswer: e.target.value })} />
        <label className="form-label">Confirm security answer</label>
        <input className="form-control" type="password" autoComplete="off" minLength="2" maxLength="100" required value={form.confirmSecurityAnswer} onChange={e => setForm({ ...form, confirmSecurityAnswer: e.target.value })} />
      </div>

      <button className="btn btn-primary btn-lg w-100" disabled={busy}>{busy ? 'Creating...' : 'Create account'}</button>
    </form>
    <p className="text-center mt-4 mb-0">Already registered? <Link to="/login">Sign in</Link></p>
  </div></div>
}
