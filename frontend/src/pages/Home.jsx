import { Link } from 'react-router-dom'

export default function Home() {
  return <>
    <section className="hero-section">
      <div className="container py-5">
        <div className="row align-items-center min-vh-75 g-5">
          <div className="col-lg-7">
            <div className="eyebrow mb-3">Learn • Challenge • Improve</div>
            <h1 className="display-3 fw-bold hero-title">Turn every question into <span>progress.</span></h1>
            <p className="lead text-secondary mt-4 col-lg-10">QuizSphere is a secure, interactive quiz platform with timed challenges, instant scoring, detailed answer review, and an admin authoring studio.</p>
            <div className="d-flex flex-wrap gap-3 mt-4">
              <Link className="btn btn-primary btn-lg rounded-pill px-5" to="/quizzes">Explore quizzes</Link>
              <Link className="btn btn-outline-dark btn-lg rounded-pill px-5" to="/register">Create account</Link>
            </div>
            <div className="d-flex flex-wrap gap-4 mt-5 text-secondary small">
              <span><i className="bi bi-shield-check me-2" />JWT secured</span>
              <span><i className="bi bi-lightning-charge me-2" />Instant results</span>
              <span><i className="bi bi-graph-up-arrow me-2" />Progress history</span>
            </div>
          </div>
          <div className="col-lg-5">
            <div className="hero-card glass-card">
              <div className="d-flex justify-content-between"><span className="badge text-bg-light">Live preview</span><span className="timer-chip">09:42</span></div>
              <div className="question-number mt-4">Question 3 of 10</div>
              <h3 className="mt-2">Which Java collection stores unique values?</h3>
              {['ArrayList', 'HashSet', 'LinkedList', 'ArrayDeque'].map((x, i) => <div key={x} className={`mock-option ${i === 1 ? 'selected' : ''}`}><span>{String.fromCharCode(65+i)}</span>{x}</div>)}
              <div className="progress mt-4" role="progressbar"><div className="progress-bar" style={{width:'30%'}} /></div>
            </div>
          </div>
        </div>
      </div>
    </section>
    <section className="container py-5">
      <div className="row g-4">
        {[['bi-stopwatch','Timed experiences','Stay focused with a clear countdown and automatic submission.'],['bi-check2-circle','Trusted scoring','Answers are evaluated securely on the Spring Boot backend.'],['bi-journal-check','Actionable review','Understand mistakes with correct answers and explanations.']].map(([icon,title,text])=><div className="col-md-4" key={title}><div className="feature-card h-100"><i className={`bi ${icon}`} /><h4>{title}</h4><p>{text}</p></div></div>)}
      </div>
    </section>
  </>
}
