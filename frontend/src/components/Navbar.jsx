import { Link, NavLink, useNavigate } from 'react-router-dom';
import { Menu, X } from 'lucide-react';
import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

const linkClass = ({ isActive }) =>
  `rounded-full px-3 py-2 text-sm font-medium ${isActive ? 'bg-sand text-forest' : 'text-ink/80 hover:text-ink'}`;

export default function Navbar() {
  const { user, logout } = useAuth();
  const notify = useToast();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);

  function signOut() {
    logout();
    notify('You are signed out.');
    setOpen(false);
    navigate('/');
  }

  const listTarget = user?.role === 'HOST' ? '/host/list' : user ? '/host/list' : '/signup?role=HOST';

  return (
    <header className="sticky top-0 z-30 border-b border-line/80 bg-cream/90 backdrop-blur">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
        <Link to="/" className="font-serif text-2xl tracking-tight text-forest">
          ParkDeck
        </Link>
        <nav className="hidden items-center gap-1 md:flex">
          <NavLink to="/explore" className={linkClass}>Explore</NavLink>
          {user && <NavLink to="/wishlist" className={linkClass}>Wishlist</NavLink>}
          {user?.role === 'DRIVER' && <NavLink to="/bookings" className={linkClass}>Bookings</NavLink>}
          {user?.role === 'HOST' && <NavLink to="/host" className={linkClass}>Host</NavLink>}
          {user && <NavLink to="/profile" className={linkClass}>Profile</NavLink>}
        </nav>
        <div className="hidden items-center gap-2 md:flex">
          <Link to={listTarget} className="rounded-full border border-forest px-4 py-2 text-sm font-semibold text-forest">
            List your space
          </Link>
          {user ? (
            <button type="button" onClick={signOut} className="rounded-full bg-forest px-4 py-2 text-sm font-semibold text-cream">
              Log out
            </button>
          ) : (
            <Link to="/login" className="rounded-full bg-forest px-4 py-2 text-sm font-semibold text-cream">
              Log in
            </Link>
          )}
        </div>
        <button type="button" className="md:hidden" aria-label="Open menu" onClick={() => setOpen((value) => !value)}>
          {open ? <X /> : <Menu />}
        </button>
      </div>
      {open && (
        <div className="space-y-2 border-t border-line px-4 py-4 md:hidden">
          <Link to="/explore" onClick={() => setOpen(false)} className="block">Explore</Link>
          {user && <Link to="/wishlist" onClick={() => setOpen(false)} className="block">Wishlist</Link>}
          {user?.role === 'DRIVER' && <Link to="/bookings" onClick={() => setOpen(false)} className="block">Bookings</Link>}
          {user?.role === 'HOST' && <Link to="/host" onClick={() => setOpen(false)} className="block">Host dashboard</Link>}
          {user && <Link to="/profile" onClick={() => setOpen(false)} className="block">Profile</Link>}
          <Link to={listTarget} onClick={() => setOpen(false)} className="block">List your space</Link>
          {user ? (
            <button type="button" onClick={signOut} className="block text-left">Log out</button>
          ) : (
            <Link to="/login" onClick={() => setOpen(false)} className="block">Log in</Link>
          )}
        </div>
      )}
    </header>
  );
}
