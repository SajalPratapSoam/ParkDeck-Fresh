import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { ApiError } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

export default function Login() {
  const { login } = useAuth();
  const notify = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  function update(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  async function submit(event) {
    event.preventDefault();
    setBusy(true);
    setError('');
    try {
      const user = await login(form.email.trim(), form.password);
      notify(`Welcome back, ${user.fullName}.`);
      const destination = location.state?.from || (user.role === 'HOST' ? '/host' : '/explore');
      navigate(destination);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Login failed.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="mx-auto max-w-md rounded-[32px] bg-paper p-6 shadow-sm sm:p-8">
      <h1 className="text-4xl">Log in</h1>
      <p className="mt-2 text-sm text-muted">
        Demo driver: meera.driver@parkdeck.local · Demo host: aarav.host@parkdeck.local · Password Parkdeck1
      </p>
      <form onSubmit={submit} className="mt-6 space-y-4">
        <label className="block text-sm font-medium">
          Email
          <input required type="email" name="email" value={form.email} onChange={update} className="mt-1 w-full rounded-2xl border border-line px-3 py-3" />
        </label>
        <label className="block text-sm font-medium">
          Password
          <input required type="password" name="password" value={form.password} onChange={update} className="mt-1 w-full rounded-2xl border border-line px-3 py-3" />
        </label>
        {error && <p className="text-sm text-red-800">{error}</p>}
        <button disabled={busy} className="w-full rounded-full bg-forest px-4 py-3 text-sm font-semibold text-cream disabled:opacity-60">
          {busy ? 'Signing in…' : 'Log in'}
        </button>
      </form>
      <p className="mt-4 text-sm">
        New here? <Link to="/signup" className="font-semibold text-forest">Create an account</Link>
      </p>
    </div>
  );
}
