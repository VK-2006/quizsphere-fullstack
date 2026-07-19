import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function AppNavbar() {
  const { isAuthenticated, isAdmin, user, logout } = useAuth()
  const navigate = useNavigate()
  const signOut = () => { logout(); navigate('/') }
  const navClass = ({ isActive }) => `nav-link ${isActive ? 'active' : ''}`
  const initials = user?.fullName?.split(' ').map(x => x[0]).join('').slice(0, 2).toUpperCase()
  return <nav className="navbar navbar-expand-lg navbar-dark app-navbar sticky-top"><div className="container">
    <Link className="navbar-brand fw-bold" to="/"><span className="brand-mark">Q</span> QuizSphere</Link>
    <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav"><span className="navbar-toggler-icon" /></button>
    <div className="collapse navbar-collapse" id="mainNav"><div className="navbar-nav ms-auto align-items-lg-center gap-lg-2">
      <NavLink className={navClass} to="/quizzes">Quizzes</NavLink>
      {isAuthenticated && <NavLink className={navClass} to="/dashboard">Dashboard</NavLink>}
      {isAuthenticated && <NavLink className={navClass} to="/history">History</NavLink>}
      {isAdmin && <NavLink className={navClass} to="/admin">Admin</NavLink>}
      {!isAuthenticated ? <>
        <NavLink className={navClass} to="/login">Login</NavLink><Link className="btn btn-primary rounded-pill px-4" to="/register">Get started</Link>
      </> : <>
        <Link to="/profile" className="navbar-profile" title="Open profile">
          <span className="nav-avatar">{user.avatarUrl ? <img src={user.avatarUrl} alt="" /> : initials}</span><span className="d-none d-lg-inline">{user.fullName}</span>
        </Link>
        <button className="btn btn-outline-light btn-sm rounded-pill px-3" onClick={signOut}>Logout</button>
      </>}
    </div></div>
  </div></nav>
}
