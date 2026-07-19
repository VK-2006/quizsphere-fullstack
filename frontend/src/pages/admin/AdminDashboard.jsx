import { useEffect, useState } from 'react'
import PageHeader from '../../components/PageHeader'
import AdminTabs from '../../components/AdminTabs'
import Loading from '../../components/Loading'
import api from '../../services/api'

export default function AdminDashboard(){const [data,setData]=useState(null);useEffect(()=>{api.get('/admin/dashboard').then(r=>setData(r.data))},[]);return <div className="container py-5"><PageHeader eyebrow="Administration" title="Content command center" subtitle="Create quizzes, manage learners, and monitor platform activity."/><AdminTabs/>{!data?<Loading/>:<div className="row g-4 mt-1">{[['Users',data.users,'bi-people'],['Categories',data.categories,'bi-grid'],['Quizzes',data.quizzes,'bi-ui-checks-grid'],['Attempts',data.attempts,'bi-bar-chart']].map(([x,n,i])=><div className="col-sm-6 col-xl-3" key={x}><div className="stat-card"><i className={`bi ${i}`}/><div><span>{x}</span><strong>{n}</strong></div></div></div>)}</div>}<div className="panel mt-4"><h3>Admin workflow</h3><ol className="admin-steps"><li>Create a category.</li><li>Create a quiz and set its duration/pass percentage.</li><li>Add questions with exactly one correct answer.</li><li>Publish the quiz when it is ready.</li></ol></div></div>}
