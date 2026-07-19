import { Link } from 'react-router-dom'
export default function NotFound(){return <div className="container text-center py-5"><div className="not-found">404</div><h1>Page not found</h1><p className="text-secondary">The page you requested does not exist.</p><Link className="btn btn-primary" to="/">Return home</Link></div>}
