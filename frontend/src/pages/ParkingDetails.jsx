import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api, ApiError } from '../api/client';
import ConfirmDialog from '../components/ConfirmDialog';
import StateMessage from '../components/StateMessage';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { clock, hoursLabel, inr, typeLabel } from '../lib/format';
import { quote } from '../lib/pricing';

function defaultRange() {
  const start = new Date();
  start.setDate(start.getDate() + 1);
  start.setHours(10, 0, 0, 0);
  const end = new Date(start);
  end.setHours(11, 30, 0, 0);
  const stamp = (date) => {
    const pad = (n) => String(n).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
  };
  return { start: stamp(start), end: stamp(end) };
}

export default function ParkingDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const notify = useToast();
  const [space, setSpace] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [times, setTimes] = useState(defaultRange);
  const [open, setOpen] = useState(false);
  const [busy, setBusy] = useState(false);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    api(`/api/spaces/${id}`, { auth: Boolean(user) })
      .then((data) => {
        if (!cancelled) setSpace(data);
      })
      .catch((err) => {
        if (!cancelled) setError(err.message);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id, user]);

  useEffect(() => {
    if (!user) return;
    api('/api/wishlist')
      .then((items) => setSaved(items.some((item) => String(item.id) === String(id))))
      .catch(() => setSaved(false));
  }, [user, id]);

  const estimate = useMemo(
    () => (space ? quote(space.hourlyPrice, times.start, times.end) : null),
    [space, times],
  );

  async function toggleSave() {
    if (!user) {
      navigate('/login', { state: { from: `/spaces/${id}` } });
      return;
    }
    try {
      if (saved) {
        await api(`/api/wishlist/${id}`, { method: 'DELETE' });
        setSaved(false);
        notify('Removed from your wishlist.');
      } else {
        await api(`/api/wishlist/${id}`, { method: 'POST' });
        setSaved(true);
        notify('Saved to your wishlist.');
      }
    } catch (err) {
      notify(err.message);
    }
  }

  function review(event) {
    event.preventDefault();
    if (user?.role !== 'DRIVER') {
      navigate(user ? '/signup?role=DRIVER' : '/login', { state: { from: `/spaces/${id}` } });
      notify(user ? 'Bookings are placed from a driver account.' : 'Log in with a driver account to reserve this space.');
      return;
    }
    if (!estimate) {
      notify('Choose an end time after the start time.');
      return;
    }
    setOpen(true);
  }

  async function confirmBooking() {
    setBusy(true);
    try {
      const booking = await api('/api/bookings', {
        method: 'POST',
        body: {
          parkingSpaceId: Number(id),
          startDateTime: times.start,
          endDateTime: times.end,
        },
      });
      setOpen(false);
      notify(`Reserved. Server total ${inr(booking.totalPrice)}. This is not a payment.`);
      navigate('/bookings');
    } catch (err) {
      setOpen(false);
      notify(err instanceof ApiError ? err.message : 'Booking failed.');
    } finally {
      setBusy(false);
    }
  }

  if (loading) return <StateMessage loading title="Opening this space" />;
  if (error) return <StateMessage title="Space unavailable" body={error} action={{ to: '/explore', label: 'Back to explore' }} />;
  if (!space) return null;

  return (
    <div className="grid gap-8 lg:grid-cols-[1.3fr_0.7fr]">
      <div>
        <img src={space.imageUrl} alt="" className="h-80 w-full rounded-[32px] object-cover" />
        <p className="mt-6 text-sm font-semibold uppercase tracking-wide text-gold">{typeLabel(space.parkingType)} · {space.city}</p>
        <h1 className="mt-2 text-5xl">{space.title}</h1>
        <p className="mt-3 text-muted">{space.address}</p>
        <p className="mt-6 max-w-2xl leading-7">{space.description}</p>
        <dl className="mt-6 grid gap-4 sm:grid-cols-3">
          <div className="rounded-3xl bg-paper p-4">
            <dt className="text-xs uppercase tracking-wide text-muted">Hourly price</dt>
            <dd className="mt-1 text-2xl font-semibold text-forest">{inr(space.hourlyPrice)}</dd>
          </div>
          <div className="rounded-3xl bg-paper p-4">
            <dt className="text-xs uppercase tracking-wide text-muted">Hours</dt>
            <dd className="mt-1 text-2xl">{clock(space.availableFrom)}–{clock(space.availableUntil)}</dd>
          </div>
          <div className="rounded-3xl bg-paper p-4">
            <dt className="text-xs uppercase tracking-wide text-muted">Host</dt>
            <dd className="mt-1 text-2xl">{space.hostName}</dd>
          </div>
        </dl>
      </div>
      <aside className="h-fit rounded-[32px] border border-line bg-paper p-5 shadow-sm">
        <div className="flex items-center justify-between">
          <h2 className="text-3xl">Reserve</h2>
          <button type="button" onClick={toggleSave} className="text-sm font-semibold text-forest">
            {saved ? 'Saved' : 'Save'}
          </button>
        </div>
        <p className="mt-2 text-sm text-muted">
          Partial hours are billed proportionally, then a 5% service fee is added. The server total is the reservation record. No payment is taken.
        </p>
        <form onSubmit={review} className="mt-4 space-y-3">
          <label className="block text-sm font-medium">
            Start
            <input
              required
              type="datetime-local"
              value={times.start}
              onChange={(event) => setTimes((current) => ({ ...current, start: event.target.value }))}
              className="mt-1 w-full rounded-2xl border border-line px-3 py-3"
            />
          </label>
          <label className="block text-sm font-medium">
            End
            <input
              required
              type="datetime-local"
              value={times.end}
              onChange={(event) => setTimes((current) => ({ ...current, end: event.target.value }))}
              className="mt-1 w-full rounded-2xl border border-line px-3 py-3"
            />
          </label>
          {estimate ? (
            <div className="rounded-2xl bg-cream px-4 py-3 text-sm leading-6">
              <p>{hoursLabel(estimate.minutes)} · subtotal {inr(estimate.subtotal)}</p>
              <p>Service fee {inr(estimate.serviceFee)}</p>
              <p className="font-semibold text-forest">Estimated total {inr(estimate.total)}</p>
            </div>
          ) : (
            <p className="text-sm text-red-800">End time has to be after the start time.</p>
          )}
          {user?.role === 'HOST' ? (
            <Link to="/signup?role=DRIVER" className="block rounded-full bg-forest px-4 py-3 text-center text-sm font-semibold text-cream">
              Use a driver account to book
            </Link>
          ) : (
            <button type="submit" className="w-full rounded-full bg-forest px-4 py-3 text-sm font-semibold text-cream">
              {user ? 'Review reservation' : 'Log in to reserve'}
            </button>
          )}
        </form>
      </aside>
      <ConfirmDialog
        open={open}
        title="Confirm this reservation"
        body={estimate
          ? `${space.title}, ${hoursLabel(estimate.minutes)}. Estimated total ${inr(estimate.total)} including the 5% fee. The backend recalculates the price and rejects overlaps.`
          : ''}
        confirmLabel="Confirm reservation"
        busy={busy}
        onClose={() => setOpen(false)}
        onConfirm={confirmBooking}
      />
    </div>
  );
}
