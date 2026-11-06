import { useEffect, useState } from 'react';
import { api } from '../api/client';
import StateMessage from '../components/StateMessage';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

export default function Profile() {
  const { user, updateUser } = useAuth();
  const notify = useToast();
  const [profile, setProfile] = useState({ fullName: '', phone: '', currentPassword: '', newPassword: '' });
  const [vehicles, setVehicles] = useState([]);
  const [vehicle, setVehicle] = useState({ vehicleType: 'Hatchback', registrationNumber: '', nickname: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([api('/api/profile'), api('/api/vehicles')])
      .then(([me, cars]) => {
        setProfile((current) => ({ ...current, fullName: me.fullName, phone: me.phone || '' }));
        setVehicles(cars);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  async function saveProfile(event) {
    event.preventDefault();
    setError('');
    const body = { fullName: profile.fullName.trim(), phone: profile.phone.trim() || null };
    if (profile.newPassword) {
      body.currentPassword = profile.currentPassword;
      body.newPassword = profile.newPassword;
    }
    try {
      const updated = await api('/api/profile', { method: 'PUT', body });
      updateUser(updated);
      setProfile((current) => ({ ...current, currentPassword: '', newPassword: '' }));
      notify('Profile saved.');
    } catch (err) {
      setError(err.message);
    }
  }

  async function addVehicle(event) {
    event.preventDefault();
    try {
      const created = await api('/api/vehicles', { method: 'POST', body: vehicle });
      setVehicles((current) => [created, ...current]);
      setVehicle({ vehicleType: 'Hatchback', registrationNumber: '', nickname: '' });
      notify('Vehicle saved.');
    } catch (err) {
      notify(err.message);
    }
  }

  async function removeVehicle(id) {
    try {
      await api(`/api/vehicles/${id}`, { method: 'DELETE' });
      setVehicles((current) => current.filter((item) => item.id !== id));
      notify('Vehicle removed.');
    } catch (err) {
      notify(err.message);
    }
  }

  if (loading) return <StateMessage loading title="Loading your profile" />;

  return (
    <div className="grid gap-8 lg:grid-cols-2">
      <section className="rounded-[32px] bg-paper p-6">
        <h1 className="text-4xl">Profile</h1>
        <p className="mt-2 text-sm text-muted">{user?.email} · {user?.role}</p>
        <form onSubmit={saveProfile} className="mt-5 space-y-3">
          <label className="block text-sm font-medium">
            Full name
            <input required value={profile.fullName} onChange={(event) => setProfile({ ...profile, fullName: event.target.value })} className="mt-1 w-full rounded-2xl border border-line px-3 py-3" />
          </label>
          <label className="block text-sm font-medium">
            Phone
            <input value={profile.phone} onChange={(event) => setProfile({ ...profile, phone: event.target.value })} className="mt-1 w-full rounded-2xl border border-line px-3 py-3" />
          </label>
          <label className="block text-sm font-medium">
            Current password
            <input type="password" value={profile.currentPassword} onChange={(event) => setProfile({ ...profile, currentPassword: event.target.value })} className="mt-1 w-full rounded-2xl border border-line px-3 py-3" />
          </label>
          <label className="block text-sm font-medium">
            New password
            <input type="password" minLength={8} value={profile.newPassword} onChange={(event) => setProfile({ ...profile, newPassword: event.target.value })} className="mt-1 w-full rounded-2xl border border-line px-3 py-3" />
          </label>
          <p className="text-xs text-muted">Leave the password fields empty to keep your current password. A change requires the current password.</p>
          {error && <p className="text-sm text-red-800">{error}</p>}
          <button className="rounded-full bg-forest px-5 py-3 text-sm font-semibold text-cream">Save profile</button>
        </form>
      </section>
      <section className="rounded-[32px] bg-paper p-6">
        <h2 className="text-3xl">Vehicles</h2>
        <form onSubmit={addVehicle} className="mt-4 space-y-3">
          <label className="block text-sm font-medium">
            Type
            <input required value={vehicle.vehicleType} onChange={(event) => setVehicle({ ...vehicle, vehicleType: event.target.value })} className="mt-1 w-full rounded-2xl border border-line px-3 py-3" />
          </label>
          <label className="block text-sm font-medium">
            Registration
            <input required value={vehicle.registrationNumber} onChange={(event) => setVehicle({ ...vehicle, registrationNumber: event.target.value })} placeholder="UP16AB1234" className="mt-1 w-full rounded-2xl border border-line px-3 py-3" />
          </label>
          <label className="block text-sm font-medium">
            Nickname
            <input value={vehicle.nickname} onChange={(event) => setVehicle({ ...vehicle, nickname: event.target.value })} className="mt-1 w-full rounded-2xl border border-line px-3 py-3" />
          </label>
          <button className="rounded-full bg-forest px-5 py-3 text-sm font-semibold text-cream">Add vehicle</button>
        </form>
        <ul className="mt-6 space-y-3">
          {vehicles.length === 0 && <li className="text-sm text-muted">No vehicles saved.</li>}
          {vehicles.map((item) => (
            <li key={item.id} className="flex items-center justify-between rounded-2xl bg-cream px-4 py-3">
              <div>
                <p className="font-semibold">{item.registrationNumber}</p>
                <p className="text-sm text-muted">{item.vehicleType}{item.nickname ? ` · ${item.nickname}` : ''}</p>
              </div>
              <button type="button" onClick={() => removeVehicle(item.id)} className="text-sm font-semibold text-forest">Remove</button>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
