import { useEffect, useState } from 'react'
import api, { errorMessage } from '../services/api'
import { useAuth } from '../context/AuthContext'
import Loading from '../components/Loading'
import { SECURITY_QUESTIONS } from '../constants/securityQuestions'

const empty = { fullName: '', bio: '', phone: '', location: '', dateOfBirth: '', avatarUrl: '' }
const emptyRecovery = {
  currentPassword: '',
  securityQuestion: SECURITY_QUESTIONS[0],
  securityAnswer: '',
  confirmSecurityAnswer: ''
}

export default function Profile() {
  const [profile, setProfile] = useState(null)
  const [form, setForm] = useState(empty)
  const [recoveryStatus, setRecoveryStatus] = useState(null)
  const [recoveryForm, setRecoveryForm] = useState(emptyRecovery)
  const [recoveryCode, setRecoveryCode] = useState('')
  const [copied, setCopied] = useState(false)
  const [busy, setBusy] = useState(false)
  const [recoveryBusy, setRecoveryBusy] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [recoveryError, setRecoveryError] = useState('')
  const [recoverySuccess, setRecoverySuccess] = useState('')
  const { updateSession } = useAuth()

  useEffect(() => {
    Promise.all([api.get('/profile'), api.get('/profile/recovery')])
      .then(([profileResponse, recoveryResponse]) => {
        const data = profileResponse.data
        setProfile(data)
        setForm({ ...empty, ...data, dateOfBirth: data.dateOfBirth || '' })
        setRecoveryStatus(recoveryResponse.data)
        setRecoveryForm(current => ({
          ...current,
          securityQuestion: recoveryResponse.data.question || SECURITY_QUESTIONS[0]
        }))
      })
      .catch(e => setError(errorMessage(e)))
  }, [])

  if (!profile && !error) return <Loading />

  const save = async event => {
    event.preventDefault(); setBusy(true); setError(''); setSuccess('')
    try {
      const { data } = await api.put('/profile', form)
      setProfile(data)
      setForm({ ...empty, ...data, dateOfBirth: data.dateOfBirth || '' })
      updateSession(data)
      setSuccess('Profile updated successfully.')
    } catch (e) {
      setError(errorMessage(e))
    } finally { setBusy(false) }
  }

  const saveRecovery = async event => {
    event.preventDefault()
    setRecoveryError(''); setRecoverySuccess(''); setRecoveryCode('')
    if (recoveryForm.securityAnswer.trim().toLowerCase() !== recoveryForm.confirmSecurityAnswer.trim().toLowerCase()) {
      setRecoveryError('Security answer and confirmation do not match')
      return
    }

    setRecoveryBusy(true)
    try {
      const { confirmSecurityAnswer, ...payload } = recoveryForm
      const { data } = await api.put('/profile/recovery', payload)
      setRecoveryStatus(data)
      setRecoveryCode(data.recoveryCode)
      setRecoverySuccess(data.message)
      setRecoveryForm({
        ...emptyRecovery,
        securityQuestion: data.question || SECURITY_QUESTIONS[0]
      })
    } catch (e) {
      setRecoveryError(errorMessage(e))
    } finally { setRecoveryBusy(false) }
  }

  const copyCode = async () => {
    await navigator.clipboard.writeText(recoveryCode)
    setCopied(true)
    setTimeout(() => setCopied(false), 1800)
  }

  const initials = profile?.fullName?.split(' ').map(x => x[0]).join('').slice(0, 2).toUpperCase()

  return <div className="container py-5">
    {error && <div className="alert alert-danger">{error}</div>}
    {success && <div className="alert alert-success">{success}</div>}
    {profile && <div className="profile-layout">
      <aside className="profile-summary card border-0 shadow-sm">
        <div className="profile-avatar-xl">{profile.avatarUrl ? <img src={profile.avatarUrl} alt={profile.fullName} /> : initials}</div>
        <h2>{profile.fullName}</h2><p className="text-secondary mb-2">{profile.email}</p>
        <span className="provider-badge"><i className="bi bi-shield-check" /> {profile.authProvider}</span>
        <div className="profile-stats">
          <div><strong>{profile.totalAttempts}</strong><span>Attempts</span></div>
          <div><strong>{profile.passedAttempts}</strong><span>Passed</span></div>
          <div><strong>{profile.averageScore}%</strong><span>Average</span></div>
        </div>
        <small className="text-secondary">Member since {profile.joinedAt ? new Date(profile.joinedAt).toLocaleDateString() : '—'}</small>
      </aside>

      <div className="d-grid gap-4">
        <section className="card border-0 shadow-sm profile-form-card">
          <div className="card-body p-4 p-lg-5"><div className="eyebrow">Account settings</div><h1 className="h2 mb-4">Edit profile</h1>
            <form onSubmit={save}>
              <div className="row g-3">
                <div className="col-md-6"><label className="form-label">Full name</label><input className="form-control" required minLength="2" maxLength="100" value={form.fullName || ''} onChange={e => setForm({ ...form, fullName: e.target.value })} /></div>
                <div className="col-md-6"><label className="form-label">Email</label><input className="form-control" disabled value={profile.email} /><div className="form-text">Email is managed by your sign-in account.</div></div>
                <div className="col-md-6"><label className="form-label">Phone</label><input className="form-control" placeholder="+91 98765 43210" value={form.phone || ''} onChange={e => setForm({ ...form, phone: e.target.value })} /></div>
                <div className="col-md-6"><label className="form-label">Location</label><input className="form-control" placeholder="Vijayawada, India" maxLength="120" value={form.location || ''} onChange={e => setForm({ ...form, location: e.target.value })} /></div>
                <div className="col-md-6"><label className="form-label">Date of birth</label><input className="form-control" type="date" max={new Date().toISOString().slice(0,10)} value={form.dateOfBirth || ''} onChange={e => setForm({ ...form, dateOfBirth: e.target.value })} /></div>
                <div className="col-md-6"><label className="form-label">Profile image URL</label><input className="form-control" type="url" placeholder="https://..." value={form.avatarUrl || ''} onChange={e => setForm({ ...form, avatarUrl: e.target.value })} /></div>
                <div className="col-12"><label className="form-label">Bio</label><textarea className="form-control" rows="4" maxLength="500" placeholder="Tell other learners a little about yourself..." value={form.bio || ''} onChange={e => setForm({ ...form, bio: e.target.value })} /><div className="form-text text-end">{(form.bio || '').length}/500</div></div>
              </div>
              <button className="btn btn-primary mt-4 px-4" disabled={busy}>{busy ? 'Saving...' : 'Save profile'}</button>
            </form>
          </div>
        </section>

        <section className="card border-0 shadow-sm profile-form-card">
          <div className="card-body p-4 p-lg-5">
            <div className="d-flex justify-content-between align-items-start gap-3 flex-wrap mb-4">
              <div><div className="eyebrow">Security</div><h2 className="h3 mb-1">Account recovery</h2><p className="text-secondary mb-0">Email-free password recovery using a question and a private code.</p></div>
              <span className={`recovery-status ${recoveryStatus?.configured ? 'configured' : 'missing'}`}>
                <i className={`bi ${recoveryStatus?.configured ? 'bi-shield-check' : 'bi-shield-exclamation'}`} />
                {recoveryStatus?.configured ? 'Configured' : 'Not configured'}
              </span>
            </div>

            {recoveryError && <div className="alert alert-danger">{recoveryError}</div>}
            {recoverySuccess && <div className="alert alert-success">{recoverySuccess}</div>}

            {recoveryCode && <div className="recovery-code-panel mb-4">
              <strong>Save your new recovery code now</strong>
              <p className="small text-secondary mb-2">The previous code is invalid. This code will not be shown again.</p>
              <div className="recovery-code-box">{recoveryCode}</div>
              <button type="button" className="btn btn-outline-primary w-100" onClick={copyCode}>
                <i className={`bi ${copied ? 'bi-check-lg' : 'bi-copy'} me-2`} />{copied ? 'Copied' : 'Copy recovery code'}
              </button>
            </div>}

            {recoveryStatus?.configured && <div className="security-question-card mb-4"><i className="bi bi-patch-check" /><div><small>Current question</small><strong>{recoveryStatus.question}</strong></div></div>}

            <form onSubmit={saveRecovery}>
              {profile.authProvider !== 'GOOGLE' && <div className="mb-3">
                <label className="form-label">Current account password</label>
                <input className="form-control" type="password" autoComplete="current-password" required value={recoveryForm.currentPassword}
                  onChange={e => setRecoveryForm({ ...recoveryForm, currentPassword: e.target.value })} />
                <div className="form-text">Required before changing recovery settings.</div>
              </div>}

              <label className="form-label">Security question</label>
              <select className="form-select mb-3" value={recoveryForm.securityQuestion}
                onChange={e => setRecoveryForm({ ...recoveryForm, securityQuestion: e.target.value })}>
                {SECURITY_QUESTIONS.map(question => <option key={question}>{question}</option>)}
              </select>

              <div className="row g-3">
                <div className="col-md-6"><label className="form-label">Security answer</label><input className="form-control" type="password" minLength="2" maxLength="100" required value={recoveryForm.securityAnswer} onChange={e => setRecoveryForm({ ...recoveryForm, securityAnswer: e.target.value })} /></div>
                <div className="col-md-6"><label className="form-label">Confirm answer</label><input className="form-control" type="password" minLength="2" maxLength="100" required value={recoveryForm.confirmSecurityAnswer} onChange={e => setRecoveryForm({ ...recoveryForm, confirmSecurityAnswer: e.target.value })} /></div>
              </div>

              <button className="btn btn-primary mt-4" disabled={recoveryBusy}>
                {recoveryBusy ? 'Updating...' : recoveryStatus?.configured ? 'Replace recovery settings' : 'Set up account recovery'}
              </button>
            </form>
          </div>
        </section>
      </div>
    </div>}
  </div>
}
