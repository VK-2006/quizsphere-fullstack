import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { errorMessage } from '../services/api'
import GoogleAuthButton from '../components/GoogleAuthButton'

export default function Login() {
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const submit = async event => {
    event.preventDefault()
    setBusy(true)
    setError('')

    try {
      const data = await login(form)
      navigate(
        location.state?.from?.pathname || (data.role === 'ADMIN' ? '/admin' : '/dashboard'),
        { replace: true }
      )
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="eyebrow">Welcome back</div>
        <h1>Sign in</h1>
        <p className="text-secondary">Continue your learning journey.</p>

        {error && <div className="alert alert-danger">{error}</div>}

        <GoogleAuthButton setError={setError} label="signin_with" />
        <div className="auth-divider"><span>or sign in with email</span></div>

        <form onSubmit={submit}>
          <label className="form-label">Email address</label>
          <input
            className="form-control form-control-lg mb-3"
            type="email"
            autoComplete="email"
            required
            value={form.email}
            onChange={event => setForm({ ...form, email: event.target.value })}
          />

          <div className="d-flex justify-content-between align-items-center">
            <label className="form-label">Password</label>
            <Link className="forgot-link" to="/forgot-password">Forgot password?</Link>
          </div>

          <input
            className="form-control form-control-lg mb-4"
            type="password"
            autoComplete="current-password"
            required
            value={form.password}
            onChange={event => setForm({ ...form, password: event.target.value })}
          />

          <button className="btn btn-primary btn-lg w-100" disabled={busy}>
            {busy ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        <p className="text-center mt-4 mb-0">
          New here? <Link to="/register">Create an account</Link>
        </p>
      </div>
    </div>
  )
}
