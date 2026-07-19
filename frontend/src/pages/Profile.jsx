import { useEffect, useState } from 'react'
import api, { errorMessage } from '../services/api'
import { useAuth } from '../context/AuthContext'
import Loading from '../components/Loading'

const empty = { fullName: '', bio: '', phone: '', location: '', dateOfBirth: '', avatarUrl: '' }

export default function Profile() {
  const [profile, setProfile] = useState(null)
  const [form, setForm] = useState(empty)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const { updateSession } = useAuth()

  useEffect(() => { api.get('/profile').then(({ data }) => { setProfile(data); setForm({ ...empty, ...data, dateOfBirth: data.dateOfBirth || '' }) }).catch(e => setError(errorMessage(e))) }, [])
  if (!profile && !error) return <Loading />

  const save = async e => {
    e.preventDefault(); setBusy(true); setError(''); setSuccess('')
    try {
      const { data } = await api.put('/profile', form)
      setProfile(data); setForm({ ...empty, ...data, dateOfBirth: data.dateOfBirth || '' }); updateSession(data); setSuccess('Profile updated successfully.')
    } catch (e) { setError(errorMessage(e)) } finally { setBusy(false) }
  }
  const initials = profile?.fullName?.split(' ').map(x => x[0]).join('').slice(0, 2).toUpperCase()
  return <div className="container py-5">
    {error && <div className="alert alert-danger">{error}</div>}{success && <div className="alert alert-success">{success}</div>}
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
    </div>}
  </div>
}
