import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { errorMessage } from '../services/api'
import GoogleAuthButton from '../components/GoogleAuthButton'

export default function Register() {
  const [form, setForm] = useState({ fullName: '', email: '', password: '' })
  const [error, setError] = useState(''); const [busy, setBusy] = useState(false)
  const { register } = useAuth(); const navigate = useNavigate()
  const submit = async e => { e.preventDefault(); setBusy(true); setError(''); try { await register(form); navigate('/dashboard') } catch (err) { setError(errorMessage(err)) } finally { setBusy(false) } }
  return <div className="auth-page"><div className="auth-card">
    <div className="eyebrow">Join QuizSphere</div><h1>Create account</h1><p className="text-secondary">Track scores and review every answer.</p>
    {error && <div className="alert alert-danger">{error}</div>}
    <GoogleAuthButton setError={setError} label="signup_with" />
    <div className="auth-divider"><span>or create with email</span></div>
    <form onSubmit={submit}>
      <label className="form-label">Full name</label><input className="form-control form-control-lg mb-3" minLength="2" required value={form.fullName} onChange={e => setForm({ ...form, fullName: e.target.value })} />
      <label className="form-label">Email address</label><input className="form-control form-control-lg mb-3" type="email" required value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} />
      <label className="form-label">Password</label><input className="form-control form-control-lg mb-4" type="password" minLength="6" required value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} />
      <button className="btn btn-primary btn-lg w-100" disabled={busy}>{busy ? 'Creating...' : 'Create account'}</button>
    </form><p className="text-center mt-4 mb-0">Already registered? <Link to="/login">Sign in</Link></p>
  </div></div>
}
