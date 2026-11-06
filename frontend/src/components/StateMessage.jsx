import { Link } from 'react-router-dom';

export default function StateMessage({
  loading = false,
  title,
  body,
  action,
}) {
  return (
    <div className="rounded-3xl border border-line bg-paper px-6 py-14 text-center shadow-sm">
      {loading && (
        <div className="mx-auto mb-4 h-10 w-10 animate-spin rounded-full border-2 border-sand border-t-forest" />
      )}
      <h2 className="text-3xl text-ink">{title}</h2>
      {body && <p className="mx-auto mt-3 max-w-md text-muted">{body}</p>}
      {action && (
        <Link
          to={action.to}
          className="mt-6 inline-flex rounded-full bg-forest px-5 py-2.5 text-sm font-semibold text-cream"
        >
          {action.label}
        </Link>
      )}
    </div>
  );
}
