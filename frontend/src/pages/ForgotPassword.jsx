import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api, { errorMessage } from '../services/api'

const initialPassword = { newPassword: '', confirmPassword: '' }

export default function ForgotPassword() {
  const [step, setStep] = useState(1)
  const [email, setEmail] = useState('')
  const [otp, setOtp] = useState('')
  const [resetToken, setResetToken] = useState('')
  const [passwords, setPasswords] = useState(initialPassword)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [resendSeconds, setResendSeconds] = useState(0)

  useEffect(() => {
    if (resendSeconds <= 0) return
    const timer = setInterval(() => setResendSeconds(value => Math.max(0, value - 1)), 1000)
    return () => clearInterval(timer)
  }, [resendSeconds])

  const requestOtp = async (event) => {
    event?.preventDefault()
    setBusy(true); setError(''); setMessage('')
    try {
      const { data } = await api.post('/auth/forgot-password', { email })
      setMessage(data.message)
      setStep(2)
      setOtp('')
      setResendSeconds(60)
    } catch (err) {
      setError(errorMessage(err))
    } finally { setBusy(false) }
  }

  const verifyOtp = async (event) => {
    event.preventDefault()
    setBusy(true); setError(''); setMessage('')
    try {
      const { data } = await api.post('/auth/verify-reset-otp', { email, otp })
      setResetToken(data.resetToken)
      setMessage(data.message)
      setStep(3)
    } catch (err) {
      setError(errorMessage(err))
    } finally { setBusy(false) }
  }

  const resetPassword = async (event) => {
    event.preventDefault()
    setError(''); setMessage('')
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
      setStep(4)
      setPasswords(initialPassword)
      setOtp('')
    } catch (err) {
      setError(errorMessage(err))
    } finally { setBusy(false) }
  }

  return <div className="auth-page">
    <div className="auth-card reset-card">
      <div className="eyebrow">Account recovery</div>
      <h1>{step === 1 ? 'Forgot password?' : step === 2 ? 'Enter your OTP' : step === 3 ? 'Create new password' : 'Password updated'}</h1>
      <p className="text-secondary">
        {step === 1 && 'Enter the email address used for your QuizSphere account.'}
        {step === 2 && <>We sent a 6-digit OTP to <strong>{email}</strong>.</>}
        {step === 3 && 'Choose a secure new password for your account.'}
        {step === 4 && 'Your password has been reset successfully.'}
      </p>

      <div className="reset-steps" aria-label="Password reset progress">
        {[1, 2, 3].map(number => <span key={number} className={step >= number ? 'active' : ''}>{number}</span>)}
      </div>

      {error && <div className="alert alert-danger">{error}</div>}
      {message && step !== 4 && <div className="alert alert-success">{message}</div>}

      {step === 1 && <form onSubmit={requestOtp}>
        <label className="form-label">Registered email address</label>
        <input className="form-control form-control-lg mb-4" type="email" autoComplete="email" required
          value={email} onChange={event => setEmail(event.target.value)} placeholder="you@example.com" />
        <button className="btn btn-primary btn-lg w-100" disabled={busy}>{busy ? 'Sending OTP...' : 'Send OTP'}</button>
      </form>}

      {step === 2 && <form onSubmit={verifyOtp}>
        <label className="form-label">6-digit OTP</label>
        <input className="form-control form-control-lg otp-input mb-3" type="text" inputMode="numeric"
          autoComplete="one-time-code" maxLength="6" pattern="[0-9]{6}" required value={otp}
          onChange={event => setOtp(event.target.value.replace(/\D/g, '').slice(0, 6))} placeholder="000000" />
        <button className="btn btn-primary btn-lg w-100" disabled={busy || otp.length !== 6}>{busy ? 'Verifying...' : 'Verify OTP'}</button>
        <div className="reset-actions">
          <button type="button" className="btn btn-link p-0" disabled={busy || resendSeconds > 0} onClick={requestOtp}>
            {resendSeconds > 0 ? `Resend OTP in ${resendSeconds}s` : 'Resend OTP'}
          </button>
          <button type="button" className="btn btn-link p-0" onClick={() => { setStep(1); setError(''); setMessage('') }}>Change email</button>
        </div>
      </form>}

      {step === 3 && <form onSubmit={resetPassword}>
        <label className="form-label">New password</label>
        <input className="form-control form-control-lg mb-3" type="password" autoComplete="new-password" minLength="6" required
          value={passwords.newPassword} onChange={event => setPasswords({ ...passwords, newPassword: event.target.value })} />
        <label className="form-label">Confirm new password</label>
        <input className="form-control form-control-lg mb-4" type="password" autoComplete="new-password" minLength="6" required
          value={passwords.confirmPassword} onChange={event => setPasswords({ ...passwords, confirmPassword: event.target.value })} />
        <div className="password-hint">Use at least 6 characters. A longer unique password is recommended.</div>
        <button className="btn btn-primary btn-lg w-100 mt-3" disabled={busy}>{busy ? 'Updating password...' : 'Reset password'}</button>
      </form>}

      {step === 4 && <div className="reset-success text-center">
        <div className="success-icon"><i className="bi bi-check-lg"></i></div>
        <p>{message}</p>
        <Link className="btn btn-primary btn-lg w-100" to="/login">Sign in with new password</Link>
      </div>}

      {step !== 4 && <p className="text-center mt-4 mb-0"><Link to="/login"><i className="bi bi-arrow-left me-2"></i>Back to sign in</Link></p>}
    </div>
  </div>
}
