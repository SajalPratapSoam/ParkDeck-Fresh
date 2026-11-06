import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="mt-16 border-t border-line bg-forest-deep text-cream">
      <div className="mx-auto grid max-w-6xl gap-8 px-4 py-12 md:grid-cols-3">
        <div>
          <p className="font-serif text-3xl">ParkDeck</p>
          <p className="mt-3 max-w-xs text-sm leading-6 text-cream/80">
            Hourly parking from people who already have the space. Greater Noida, Noida, and Delhi.
          </p>
        </div>
        <div className="text-sm leading-8">
          <Link to="/explore" className="block hover:underline">Explore parking</Link>
          <Link to="/bookings" className="block hover:underline">My bookings</Link>
          <Link to="/wishlist" className="block hover:underline">Wishlist</Link>
          <Link to="/profile" className="block hover:underline">Profile</Link>
        </div>
        <div className="text-sm leading-8">
          <Link to="/host" className="block hover:underline">Host dashboard</Link>
          <Link to="/host/list" className="block hover:underline">List your space</Link>
          <Link to="/signup" className="block hover:underline">Create an account</Link>
          <Link to="/login" className="block hover:underline">Log in</Link>
        </div>
      </div>
      <p className="border-t border-white/10 px-4 py-4 text-center text-xs text-cream/70">
        Reservations are records only. ParkDeck does not process payments in this version.
      </p>
    </footer>
  );
}
