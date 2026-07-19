import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import PageHeader from '../components/PageHeader'
import Loading from '../components/Loading'
import api from '../services/api'
import { useAuth } from '../context/AuthContext'

export default function Dashboard(){
 const {user}=useAuth(); const [history,setHistory]=useState(null)
 useEffect(()=>{api.get('/users/me/attempts').then(r=>setHistory(r.data)).catch(()=>setHistory([]))},[])
 if(history===null)return <Loading/>
 const completed=history.filter(x=>x.status!=='IN_PROGRESS'); const passed=completed.filter(x=>x.passed).length; const avg=completed.length?Math.round(completed.reduce((s,x)=>s+x.percentage,0)/completed.length):0
 return <div className="container py-5"><PageHeader eyebrow="Personal dashboard" title={`Welcome, ${user.fullName}`} subtitle="Keep building momentum, one quiz at a time." actions={<Link to="/quizzes" className="btn btn-primary">Take a quiz</Link>}/><div className="row g-4 mb-5">{[['Attempts',completed.length,'bi-lightning'],['Passed',passed,'bi-trophy'],['Average',`${avg}%`,'bi-graph-up']].map(([label,value,icon])=><div className="col-md-4" key={label}><div className="stat-card"><i className={`bi ${icon}`}/><div><span>{label}</span><strong>{value}</strong></div></div></div>)}</div><div className="panel"><div className="d-flex justify-content-between align-items-center mb-3"><h3 className="mb-0">Recent attempts</h3><Link to="/history">View all</Link></div>{completed.slice(0,5).map(a=><div className="attempt-row" key={a.attemptId}><div><strong>{a.quizTitle}</strong><div className="small text-secondary">{new Date(a.startedAt).toLocaleString()}</div></div><div className="text-end"><span className={`result-pill ${a.passed?'pass':'fail'}`}>{a.percentage}%</span><div><Link className="small" to={`/result/${a.attemptId}`}>View result</Link></div></div></div>)}{!completed.length&&<div className="empty-state">No completed attempts yet. Your first score will appear here.</div>}</div></div>
}
