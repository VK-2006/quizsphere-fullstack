import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const SCRIPT_ID = 'google-identity-services'

export default function GoogleAuthButton({ setError, label = 'continue_with' }) {
  const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID
  const { googleLogin } = useAuth()
  const navigate = useNavigate()
  const buttonRef = useRef(null)
  const [ready, setReady] = useState(false)

  useEffect(() => {
    if (!clientId) return
    const initialize = () => {
      if (!window.google?.accounts?.id || !buttonRef.current) return
      window.google.accounts.id.initialize({
        client_id: clientId,
        callback: async response => {
          try {
            setError('')
            const data = await googleLogin(response.credential)
            navigate(data.role === 'ADMIN' ? '/admin' : '/dashboard', { replace: true })
          } catch (error) {
            setError(error.response?.data?.message || 'Google Sign-In failed')
          }
        }
      })
      buttonRef.current.innerHTML = ''
      window.google.accounts.id.renderButton(buttonRef.current, {
        type: 'standard', theme: 'outline', size: 'large', shape: 'pill',
        text: label, width: 340, logo_alignment: 'left'
      })
      setReady(true)
    }

    if (window.google?.accounts?.id) { initialize(); return }
    let script = document.getElementById(SCRIPT_ID)
    if (!script) {
      script = document.createElement('script')
      script.id = SCRIPT_ID
      script.src = 'https://accounts.google.com/gsi/client'
      script.async = true
      script.defer = true
      document.head.appendChild(script)
    }
    script.addEventListener('load', initialize)
    script.addEventListener('error', () => setError('Could not load Google Sign-In'))
    return () => {
      script?.removeEventListener('load', initialize)
    }
  }, [clientId, googleLogin, label, navigate, setError])

  if (!clientId) return <div className="google-setup-note">Google Sign-In appears after adding VITE_GOOGLE_CLIENT_ID.</div>
  return <div className="google-login-wrap">
    {!ready && <span className="text-secondary small">Loading Google Sign-In…</span>}
    <div ref={buttonRef} />
  </div>
}
