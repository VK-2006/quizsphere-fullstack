export default function PageHeader({ eyebrow, title, subtitle, actions }) {
  return <div className="page-header d-flex flex-column flex-lg-row justify-content-between align-items-lg-end gap-3 mb-4">
    <div>{eyebrow && <div className="eyebrow">{eyebrow}</div>}<h1>{title}</h1>{subtitle && <p className="text-secondary mb-0">{subtitle}</p>}</div>
    {actions && <div>{actions}</div>}
  </div>
}
