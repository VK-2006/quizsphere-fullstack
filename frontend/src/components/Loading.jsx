export default function Loading({ text = 'Loading...' }) {
  return <div className="text-center py-5"><div className="spinner-border text-primary" role="status" /><p className="text-secondary mt-3">{text}</p></div>
}
