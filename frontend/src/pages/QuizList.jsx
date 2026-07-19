import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import PageHeader from '../components/PageHeader'
import Loading from '../components/Loading'
import api, { errorMessage } from '../services/api'
import { useAuth } from '../context/AuthContext'

export default function QuizList(){
 const [quizzes,setQuizzes]=useState([]),[error,setError]=useState(''),[loading,setLoading]=useState(true); const {isAuthenticated}=useAuth()
 useEffect(()=>{api.get('/quizzes').then(r=>setQuizzes(r.data)).catch(e=>setError(errorMessage(e))).finally(()=>setLoading(false))},[])
 return <div className="container py-5"><PageHeader eyebrow="Quiz library" title="Choose your next challenge" subtitle="Each quiz includes a timer, instant score, and detailed review." />{loading?<Loading/>:error?<div className="alert alert-danger">{error}</div>:<div className="row g-4">{quizzes.map(q=><div className="col-md-6 col-xl-4" key={q.id}><div className="quiz-card h-100"><div className="d-flex justify-content-between"><span className="category-chip">{q.categoryName}</span><span className={`difficulty ${q.difficulty.toLowerCase()}`}>{q.difficulty}</span></div><h3>{q.title}</h3><p className="text-secondary flex-grow-1">{q.description}</p><div className="quiz-meta"><span><i className="bi bi-list-check"/> {q.questionCount} questions</span><span><i className="bi bi-clock"/> {q.durationMinutes} min</span><span><i className="bi bi-award"/> {q.passPercentage}% pass</span></div><Link className="btn btn-primary w-100 mt-4" to={isAuthenticated?`/quiz/${q.id}`:'/login'}>{isAuthenticated?'Start quiz':'Login to start'}</Link></div></div>)}{!quizzes.length&&<p>No published quizzes yet.</p>}</div>}</div>
}
