import { useEffect, useState } from 'react'
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const readTheme = () => {
  const current = document.documentElement.dataset.theme
  if (current === 'dark' || current === 'light') return current

  const saved = localStorage.getItem('quizsphere-theme')
  if (saved === 'dark' || saved === 'light') return saved

  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export default function AppNavbar() {
  const { isAuthenticated, isAdmin, user, logout } = useAuth()
  const [theme, setTheme] = useState(readTheme)
  const navigate = useNavigate()
  const location = useLocation()

  useEffect(() => {
    document.documentElement.dataset.theme = theme
    document.documentElement.setAttribute('data-bs-theme', theme)
    document.documentElement.style.colorScheme = theme
    localStorage.setItem('quizsphere-theme', theme)
    document
      .querySelector('meta[name="theme-color"]')
      ?.setAttribute('content', theme === 'dark' ? '#0b1020' : '#fafbfe')
  }, [theme])

  useEffect(() => {
    closeMobileMenu()
  }, [location.pathname])

  const closeMobileMenu = () => {
    const menu = document.getElementById('mainNav')
    if (!menu?.classList.contains('show')) return
    document.querySelector('[data-bs-target="#mainNav"]')?.click()
  }

  const signOut = () => {
    closeMobileMenu()
    logout()
    navigate('/')
  }

  const toggleTheme = () => {
    document.documentElement.classList.add('theme-transitioning')
    setTheme(current => current === 'dark' ? 'light' : 'dark')
    window.setTimeout(() => {
      document.documentElement.classList.remove('theme-transitioning')
    }, 320)
  }

  const navClass = ({ isActive }) => `nav-link ${isActive ? 'active' : ''}`
  const initials = user?.fullName
    ?.split(' ')
    .map(part => part[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()

  const nextTheme = theme === 'dark' ? 'light' : 'dark'

  return (
    <nav className="navbar navbar-expand-lg navbar-dark app-navbar sticky-top" aria-label="Main navigation">
      <div className="container-fluid container-xxl px-3 px-sm-4">
        <Link className="navbar-brand fw-bold" to="/" onClick={closeMobileMenu}>
          <span className="brand-mark" aria-hidden="true">Q</span> QuizSphere
        </Link>

        <div className="d-flex align-items-center gap-2 order-lg-2">
          <button
            className="theme-toggle"
            type="button"
            onClick={toggleTheme}
            aria-label={`Switch to ${nextTheme} mode`}
            title={`Switch to ${nextTheme} mode`}
          >
            <i className={theme === 'dark' ? 'bi bi-sun-fill' : 'bi bi-moon-stars-fill'} aria-hidden="true" />
          </button>

          <button
            className="navbar-toggler"
            type="button"
            data-bs-toggle="collapse"
            data-bs-target="#mainNav"
            aria-controls="mainNav"
            aria-expanded="false"
            aria-label="Toggle navigation"
          >
            <span className="navbar-toggler-icon" />
          </button>
        </div>

        <div className="collapse navbar-collapse order-lg-1" id="mainNav">
          <div className="navbar-nav ms-auto align-items-lg-center gap-lg-2">
            <NavLink className={navClass} to="/quizzes" onClick={closeMobileMenu}>Quizzes</NavLink>
            {isAuthenticated && <NavLink className={navClass} to="/dashboard" onClick={closeMobileMenu}>Dashboard</NavLink>}
            {isAuthenticated && <NavLink className={navClass} to="/history" onClick={closeMobileMenu}>History</NavLink>}
            {isAdmin && <NavLink className={navClass} to="/admin" onClick={closeMobileMenu}>Admin</NavLink>}

            {!isAuthenticated ? (
              <>
                <NavLink className={navClass} to="/login" onClick={closeMobileMenu}>Login</NavLink>
                <Link className="btn btn-primary rounded-pill px-4 nav-cta" to="/register" onClick={closeMobileMenu}>Get started</Link>
              </>
            ) : (
              <>
                <Link to="/profile" className="navbar-profile" title="Open profile" onClick={closeMobileMenu}>
                  <span className="nav-avatar">
                    {user.avatarUrl ? <img src={user.avatarUrl} alt="" /> : initials}
                  </span>
                  <span className="d-none d-lg-inline text-truncate nav-user-name">{user.fullName}</span>
                </Link>
                <button className="btn btn-outline-light btn-sm rounded-pill px-3 nav-logout" onClick={signOut}>
                  Logout
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  )
}
