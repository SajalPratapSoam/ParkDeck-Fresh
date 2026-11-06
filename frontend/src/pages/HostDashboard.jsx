import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import ConfirmDialog from '../components/ConfirmDialog';
import StateMessage from '../components/StateMessage';
import { useToast } from '../context/ToastContext';
import { inr, when } from '../lib/format';

export default function HostDashboard() {
  const notify = useToast();
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState('');
  const [target, setTarget] = useState(null);

  async function load() {
    try {
      setSummary(await api('/api/host/summary'));
    } catch (err) {
      setError(err.message);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function deactivate() {
    try {
      await api(`/api/spaces/${target.id}`, { method: 'DELETE' });
      notify('Listing deactivated. Existing booking history is unchanged.');
      setTarget(null);
      await load();
    } catch (err) {
      notify(err.message);
    }
  }

  if (error) return <StateMessage title="Host dashboard unavailable" body={error} />;
  if (!summary) return <StateMessage loading title="Loading your listings" />;

  const cards = [
    ['Active listings', summary.activeListings],
    ['Inactive listings', summary.inactiveListings],
    ['Confirmed bookings', summary.confirmedBookings],
    ['Cancelled bookings', summary.cancelledBookings],
    ['Earnings', inr(summary.earnings)],
  ];

  return (
    <div>
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-sm font-semibold uppercase tracking-wide text-gold">Host</p>
          <h1 className="text-5xl">Your spaces</h1>
        </div>
        <Link to="/host/list" className="rounded-full bg-forest px-5 py-3 text-sm font-semibold text-cream">List a space</Link>
      </div>
      <div className="mt-6 grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
        {cards.map(([label, value]) => (
          <article key={label} className="rounded-3xl bg-paper p-4">
            <p className="text-xs uppercase tracking-wide text-muted">{label}</p>
            <p className="mt-2 text-2xl font-semibold">{value}</p>
          </article>
        ))}
      </div>
      <p className="mt-4 text-sm text-muted">{summary.earningsNote}</p>

      <h2 className="mt-10 text-3xl">Listings</h2>
      <div className="mt-4 space-y-3">
        {summary.spaces.length === 0 && <p className="text-muted">You have not listed a space yet.</p>}
        {summary.spaces.map((space) => (
          <article key={space.id} className="flex flex-wrap items-center justify-between gap-3 rounded-3xl bg-paper p-4">
            <div>
              <p className="font-semibold">{space.title}</p>
              <p className="text-sm text-muted">{space.city} · {inr(space.hourlyPrice)}/hr · {space.active ? 'Active' : 'Inactive'}</p>
            </div>
            <div className="flex gap-2">
              <Link to={`/host/spaces/${space.id}/edit`} className="rounded-full border border-forest px-4 py-2 text-sm font-semibold text-forest">Edit</Link>
              {space.active && (
                <button type="button" onClick={() => setTarget(space)} className="rounded-full px-4 py-2 text-sm font-semibold text-ink">Deactivate</button>
              )}
            </div>
          </article>
        ))}
      </div>

      <h2 className="mt-10 text-3xl">Bookings on your spaces</h2>
      <div className="mt-4 space-y-3">
        {summary.bookings.length === 0 && <p className="text-muted">No reservations yet.</p>}
        {summary.bookings.map((booking) => (
          <article key={booking.id} className="rounded-3xl bg-paper p-4">
            <p className="text-xs font-semibold uppercase tracking-wide text-gold">{booking.status}</p>
            <p className="font-semibold">{booking.spaceTitle} · {booking.driverName}</p>
            <p className="text-sm text-muted">{when(booking.startDateTime)} – {when(booking.endDateTime)} · {inr(booking.totalPrice)}</p>
          </article>
        ))}
      </div>
      <ConfirmDialog
        open={Boolean(target)}
        title="Deactivate this listing?"
        body="Drivers will no longer see it in search. Confirmed bookings stay on record."
        confirmLabel="Deactivate"
        onClose={() => setTarget(null)}
        onConfirm={deactivate}
      />
    </div>
  );
}
