import { createContext, useContext, useMemo, useState } from 'react'
import api from '../services/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => {
    const saved = localStorage.getItem('quizsphere-auth')
    return saved ? JSON.parse(saved) : null
  })

  const store = (data) => {
    localStorage.setItem('quizsphere-auth', JSON.stringify(data))
    setAuth(data)
  }

  const login = async (payload) => {
    const { data } = await api.post('/auth/login', payload)
    store(data); return data
  }

  const register = async (payload) => {
    const { data } = await api.post('/auth/register', payload)
    const { recoveryCode, ...session } = data
    store(session)
    return { ...session, recoveryCode }
  }

  const googleLogin = async (credential) => {
    const { data } = await api.post('/auth/google', { credential })
    store(data); return data
  }

  const updateSession = (profile) => {
    if (!auth) return
    store({ ...auth, fullName: profile.fullName, avatarUrl: profile.avatarUrl, authProvider: profile.authProvider })
  }

  const logout = () => {
    localStorage.removeItem('quizsphere-auth')
    setAuth(null)
  }

  const value = useMemo(() => ({
    auth, user: auth, isAuthenticated: Boolean(auth?.token), isAdmin: auth?.role === 'ADMIN',
    login, register, googleLogin, updateSession, logout
  }), [auth])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = () => useContext(AuthContext)
