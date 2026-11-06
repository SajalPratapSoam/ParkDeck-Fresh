import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api } from '../api/client';
import StateMessage from '../components/StateMessage';
import { useToast } from '../context/ToastContext';

const EMPTY = {
  title: '',
  description: '',
  address: '',
  city: 'Noida',
  parkingType: 'COVERED',
  hourlyPrice: '50',
  imageUrl: 'https://images.unsplash.com/photo-1506521781263-d8422e82f27a?auto=format&fit=crop&w=1400&q=80',
  availableFrom: '06:00',
  availableUntil: '22:00',
  active: true,
};

const SUGGESTED = [
  'https://images.unsplash.com/photo-1506521781263-d8422e82f27a?auto=format&fit=crop&w=1400&q=80',
  'https://images.unsplash.com/photo-1590674899484-d5640e854abe?auto=format&fit=crop&w=1400&q=80',
  'https://images.unsplash.com/photo-1470224114660-3f6686c562eb?auto=format&fit=crop&w=1400&q=80',
];

export default function ListSpace() {
  const { id } = useParams();
  const navigate = useNavigate();
  const notify = useToast();
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(Boolean(id));

  useEffect(() => {
    if (!id) return;
    api(`/api/spaces/${id}`)
      .then((space) => setForm({
        title: space.title,
        description: space.description,
        address: space.address,
        city: space.city,
        parkingType: space.parkingType,
        hourlyPrice: String(space.hourlyPrice),
        imageUrl: space.imageUrl,
        availableFrom: String(space.availableFrom).slice(0, 5),
        availableUntil: String(space.availableUntil).slice(0, 5),
        active: space.active,
      }))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [id]);

  function update(event) {
    const { name, value, type, checked } = event.target;
    setForm((current) => ({ ...current, [name]: type === 'checkbox' ? checked : value }));
  }

  async function submit(event) {
    event.preventDefault();
    setError('');
    const price = Number(form.hourlyPrice);
    if (!Number.isFinite(price) || price <= 0) {
      setError('Hourly price must be a positive amount in INR.');
      return;
    }
    if (form.availableFrom >= form.availableUntil) {
      setError('Available until must be later than available from.');
      return;
    }
    const body = {
      ...form,
      hourlyPrice: price.toFixed(2),
      availableFrom: `${form.availableFrom}:00`,
      availableUntil: `${form.availableUntil}:00`,
    };
    try {
      if (id) {
        await api(`/api/spaces/${id}`, { method: 'PUT', body });
        notify('Listing updated.');
      } else {
        await api('/api/spaces', { method: 'POST', body });
        notify('Listing published.');
      }
      navigate('/host');
    } catch (err) {
      setError(err.message);
    }
  }

  if (loading) return <StateMessage loading title="Loading listing" />;

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="text-5xl">{id ? 'Edit your space' : 'List your space'}</h1>
      <p className="mt-2 text-muted">Prices are rupees per hour. Drivers only see the listing while it is active.</p>
      <form onSubmit={submit} className="mt-6 space-y-4 rounded-[32px] bg-paper p-6">
        {[
          ['title', 'Title', 'text'],
          ['address', 'Address', 'text'],
          ['city', 'City', 'text'],
          ['hourlyPrice', 'Hourly price (INR)', 'number'],
          ['imageUrl', 'Image URL', 'url'],
        ].map(([name, label, type]) => (
          <label key={name} className="block text-sm font-medium">
            {label}
            <input required type={type} name={name} min={type === 'number' ? '0.01' : undefined} step="0.01" value={form[name]} onChange={update} className="mt-1 w-full rounded-2xl border border-line px-3 py-3" />
          </label>
        ))}
        <div className="flex flex-wrap gap-2">
          {SUGGESTED.map((url, index) => (
            <button key={url} type="button" onClick={() => setForm((current) => ({ ...current, imageUrl: url }))} className="rounded-full border border-line px-3 py-1 text-xs">
              Sample photo {index + 1}
            </button>
          ))}
        </div>
        <label className="block text-sm font-medium">
          Description
          <textarea required name="description" value={form.description} onChange={update} rows={4} className="mt-1 w-full rounded-2xl border border-line px-3 py-3" />
        </label>
        <label className="block text-sm font-medium">
          Parking type
          <select name="parkingType" value={form.parkingType} onChange={update} className="mt-1 w-full rounded-2xl border border-line px-3 py-3">
            {['COVERED', 'OPEN', 'BASEMENT', 'STREET'].map((type) => <option key={type}>{type}</option>)}
          </select>
        </label>
        <div className="grid gap-3 sm:grid-cols-2">
          <label className="block text-sm font-medium">
            Available from
            <input required type="time" name="availableFrom" value={form.availableFrom} onChange={update} className="mt-1 w-full rounded-2xl border border-line px-3 py-3" />
          </label>
          <label className="block text-sm font-medium">
            Available until
            <input required type="time" name="availableUntil" value={form.availableUntil} onChange={update} className="mt-1 w-full rounded-2xl border border-line px-3 py-3" />
          </label>
        </div>
        {id && (
          <label className="flex items-center gap-2 text-sm font-medium">
            <input type="checkbox" name="active" checked={form.active} onChange={update} />
            Listing is active
          </label>
        )}
        {error && <p className="text-sm text-red-800">{error}</p>}
        <button className="rounded-full bg-forest px-5 py-3 text-sm font-semibold text-cream">{id ? 'Save changes' : 'Publish listing'}</button>
      </form>
    </div>
  );
}
