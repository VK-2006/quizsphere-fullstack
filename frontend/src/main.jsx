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

const savedTheme = localStorage.getItem('quizsphere-theme')
const initialTheme = savedTheme === 'dark' || savedTheme === 'light'
  ? savedTheme
  : window.matchMedia?.('(prefers-color-scheme: dark)').matches
    ? 'dark'
    : 'light'

document.documentElement.dataset.theme = initialTheme
document.documentElement.setAttribute('data-bs-theme', initialTheme)

ReactDOM.createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <AuthProvider>
      <App />
    </AuthProvider>
  </BrowserRouter>
)
