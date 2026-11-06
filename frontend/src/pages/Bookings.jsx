import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import ConfirmDialog from '../components/ConfirmDialog';
import StateMessage from '../components/StateMessage';
import { useToast } from '../context/ToastContext';
import { hoursLabel, inr, when } from '../lib/format';

export default function Bookings() {
  const notify = useToast();
  const [bookings, setBookings] = useState(null);
  const [error, setError] = useState('');
  const [target, setTarget] = useState(null);
  const [busy, setBusy] = useState(false);

  async function load() {
    try {
      setBookings(await api('/api/bookings/my'));
    } catch (err) {
      setError(err.message);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function cancel() {
    setBusy(true);
    try {
      await api(`/api/bookings/${target.id}/cancel`, { method: 'PATCH' });
      notify('Booking cancelled. It stays in your history.');
      setTarget(null);
      await load();
    } catch (err) {
      notify(err.message);
    } finally {
      setBusy(false);
    }
  }

  if (error) return <StateMessage title="Could not load bookings" body={error} />;
  if (!bookings) return <StateMessage loading title="Loading your bookings" />;
  if (bookings.length === 0) {
    return (
      <StateMessage
        title="No bookings yet"
        body="When you reserve a space, the confirmation and any later cancellation will show up here."
        action={{ to: '/explore', label: 'Explore parking' }}
      />
    );
  }

  return (
    <div>
      <h1 className="text-5xl">My bookings</h1>
      <p className="mt-2 text-muted">Confirmed reservations can be cancelled until the end time. Cancelled rows stay in this list.</p>
      <div className="mt-6 space-y-4">
        {bookings.map((booking) => {
          const ended = new Date(booking.endDateTime).getTime() <= Date.now();
          return (
            <article key={booking.id} className="grid gap-4 rounded-[28px] bg-paper p-4 sm:grid-cols-[140px_1fr_auto]">
              <img src={booking.imageUrl} alt="" className="h-28 w-full rounded-2xl object-cover" />
              <div>
                <p className="text-xs font-semibold uppercase tracking-wide text-gold">{booking.status}</p>
                <h2 className="text-2xl">
                  <Link to={`/spaces/${booking.parkingSpaceId}`}>{booking.spaceTitle}</Link>
                </h2>
                <p className="text-sm text-muted">{booking.spaceAddress}</p>
                <p className="mt-2 text-sm">{when(booking.startDateTime)} – {when(booking.endDateTime)}</p>
                <p className="text-sm text-muted">{hoursLabel(booking.durationMinutes)} · fee {inr(booking.serviceFee)} · total {inr(booking.totalPrice)}</p>
              </div>
              {booking.status === 'CONFIRMED' && !ended ? (
                <button type="button" onClick={() => setTarget(booking)} className="h-fit rounded-full border border-forest px-4 py-2 text-sm font-semibold text-forest">
                  Cancel
                </button>
              ) : (
                <p className="text-sm text-muted">{booking.status === 'CANCELLED' ? 'Kept for history' : 'Ended'}</p>
              )}
            </article>
          );
        })}
      </div>
      <ConfirmDialog
        open={Boolean(target)}
        title="Cancel this booking?"
        body="The space becomes available again. The cancelled booking remains in your history and is excluded from host earnings."
        confirmLabel="Cancel booking"
        busy={busy}
        onClose={() => setTarget(null)}
        onConfirm={cancel}
      />
    </div>
  );
}
