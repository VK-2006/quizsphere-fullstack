import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import './styles.css'
import './theme.css'
import App from './App'
import { AuthProvider } from './context/AuthContext'

const applyTheme = theme => {
  document.documentElement.dataset.theme = theme
  document.documentElement.setAttribute('data-bs-theme', theme)
  document.documentElement.style.colorScheme = theme
  document
    .querySelector('meta[name="theme-color"]')
    ?.setAttribute('content', theme === 'dark' ? '#0b1020' : '#fafbfe')
}

const savedTheme = localStorage.getItem('quizsphere-theme')
if (savedTheme !== 'dark' && savedTheme !== 'light') {
  const media = window.matchMedia?.('(prefers-color-scheme: dark)')
  const syncWithSystem = event => applyTheme(event.matches ? 'dark' : 'light')
  media?.addEventListener?.('change', syncWithSystem)
}

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <App />
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>
)
