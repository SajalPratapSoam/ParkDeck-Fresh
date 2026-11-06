import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { ApiError } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

export default function Signup() {
  const [params] = useSearchParams();
  const { register } = useAuth();
  const notify = useToast();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    phone: '',
    role: params.get('role') === 'HOST' ? 'HOST' : 'DRIVER',
  });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  function update(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  async function submit(event) {
    event.preventDefault();
    if (form.password.length < 8) {
      setError('Password must be at least 8 characters.');
      return;
    }
    setBusy(true);
    setError('');
    try {
      const user = await register({
        fullName: form.fullName.trim(),
        email: form.email.trim(),
        password: form.password,
        phone: form.phone.trim() || null,
        role: form.role,
      });
      notify('Account created.');
      navigate(user.role === 'HOST' ? '/host/list' : '/explore');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not create the account.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="mx-auto max-w-md rounded-[32px] bg-paper p-6 shadow-sm sm:p-8">
      <h1 className="text-4xl">Create an account</h1>
      <p className="mt-2 text-sm text-muted">Drivers book spaces. Hosts list them. One account has one role.</p>
      <form onSubmit={submit} className="mt-6 space-y-4">
        <label className="block text-sm font-medium">
          Full name
          <input required name="fullName" value={form.fullName} onChange={update} className="mt-1 w-full rounded-2xl border border-line px-3 py-3" />
        </label>
        <label className="block text-sm font-medium">
          Email
          <input required type="email" name="email" value={form.email} onChange={update} className="mt-1 w-full rounded-2xl border border-line px-3 py-3" />
        </label>
        <label className="block text-sm font-medium">
          Phone, optional
          <input name="phone" value={form.phone} onChange={update} placeholder="+919876543210" className="mt-1 w-full rounded-2xl border border-line px-3 py-3" />
        </label>
        <label className="block text-sm font-medium">
          Password
          <input required type="password" minLength={8} name="password" value={form.password} onChange={update} className="mt-1 w-full rounded-2xl border border-line px-3 py-3" />
        </label>
        <fieldset>
          <legend className="text-sm font-medium">I want to</legend>
          <div className="mt-2 grid grid-cols-2 gap-2">
            {['DRIVER', 'HOST'].map((role) => (
              <label key={role} className={`rounded-2xl border px-3 py-3 text-sm ${form.role === role ? 'border-forest bg-sand' : 'border-line'}`}>
                <input type="radio" name="role" value={role} checked={form.role === role} onChange={update} className="mr-2" />
                {role === 'DRIVER' ? 'Book parking' : 'List a space'}
              </label>
            ))}
          </div>
        </fieldset>
        {error && <p className="text-sm text-red-800">{error}</p>}
        <button disabled={busy} className="w-full rounded-full bg-forest px-4 py-3 text-sm font-semibold text-cream disabled:opacity-60">
          {busy ? 'Creating…' : 'Create account'}
        </button>
      </form>
      <p className="mt-4 text-sm">
        Already registered? <Link to="/login" className="font-semibold text-forest">Log in</Link>
      </p>
    </div>
  );
}
