import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="rounded-[32px] bg-paper px-6 py-16 text-center">
      <p className="text-sm font-semibold uppercase tracking-wide text-gold">404</p>
      <h1 className="mt-2 text-5xl">That page is not on the map</h1>
      <p className="mx-auto mt-3 max-w-md text-muted">The link may be old. Explore current spaces instead.</p>
      <div className="mt-6 flex justify-center gap-3">
        <Link to="/" className="rounded-full bg-forest px-5 py-3 text-sm font-semibold text-cream">Home</Link>
        <Link to="/explore" className="rounded-full border border-forest px-5 py-3 text-sm font-semibold text-forest">Explore</Link>
      </div>
    </div>
  );
}
