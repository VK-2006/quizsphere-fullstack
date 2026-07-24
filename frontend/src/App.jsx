import { useEffect } from 'react'
import { Route, Routes, useLocation } from 'react-router-dom'
import AppNavbar from './components/AppNavbar'
import ProtectedRoute from './components/ProtectedRoute'
import Home from './pages/Home'
import Login from './pages/Login'
import Register from './pages/Register'
import ForgotPassword from './pages/ForgotPassword'
import QuizList from './pages/QuizList'
import Dashboard from './pages/Dashboard'
import QuizPlay from './pages/QuizPlay'
import Result from './pages/Result'
import Review from './pages/Review'
import History from './pages/History'
import Profile from './pages/Profile'
import AdminDashboard from './pages/admin/AdminDashboard'
import AdminCategories from './pages/admin/AdminCategories'
import AdminQuizzes from './pages/admin/AdminQuizzes'
import AdminQuestions from './pages/admin/AdminQuestions'
import AdminUsers from './pages/admin/AdminUsers'
import NotFound from './pages/NotFound'

export default function App() {
  const location = useLocation()

  useEffect(() => {
    const reduceMotion = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches
    window.scrollTo({ top: 0, left: 0, behavior: reduceMotion ? 'auto' : 'smooth' })
  }, [location.pathname])

  return (
    <div className="app-shell">
      <a className="skip-link" href="#main-content">Skip to content</a>
      <AppNavbar />
      <main id="main-content" className="app-main">
        <div className="route-stage" key={location.pathname}>
          <Routes location={location}>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/quizzes" element={<QuizList />} />
            <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
            <Route path="/quiz/:quizId" element={<ProtectedRoute><QuizPlay /></ProtectedRoute>} />
            <Route path="/result/:attemptId" element={<ProtectedRoute><Result /></ProtectedRoute>} />
            <Route path="/review/:attemptId" element={<ProtectedRoute><Review /></ProtectedRoute>} />
            <Route path="/history" element={<ProtectedRoute><History /></ProtectedRoute>} />
            <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />
            <Route path="/admin" element={<ProtectedRoute admin><AdminDashboard /></ProtectedRoute>} />
            <Route path="/admin/categories" element={<ProtectedRoute admin><AdminCategories /></ProtectedRoute>} />
            <Route path="/admin/quizzes" element={<ProtectedRoute admin><AdminQuizzes /></ProtectedRoute>} />
            <Route path="/admin/quizzes/:quizId/questions" element={<ProtectedRoute admin><AdminQuestions /></ProtectedRoute>} />
            <Route path="/admin/users" element={<ProtectedRoute admin><AdminUsers /></ProtectedRoute>} />
            <Route path="*" element={<NotFound />} />
          </Routes>
        </div>
      </main>
    </div>
  )
}
