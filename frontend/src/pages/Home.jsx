import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import ParkingCard from '../components/ParkingCard';
import SearchBar from '../components/SearchBar';
import { useAuth } from '../context/AuthContext';

const STEPS = [
  { n: '01', title: 'Find a bay', body: 'Search Greater Noida, Noida, or Delhi by neighbourhood, type, and hourly price.' },
  { n: '02', title: 'Choose the hours', body: 'The server prices partial hours proportionally and adds a 5% reservation fee.' },
  { n: '03', title: 'Drive in', body: 'Your booking is a reservation record. Payment collection is not part of this version.' },
];

export default function Home() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [featured, setFeatured] = useState([]);
  const [status, setStatus] = useState('loading');
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    api('/api/spaces?size=6', { auth: false })
      .then((page) => {
        if (!cancelled) {
          setFeatured(page.content || []);
          setStatus('ready');
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err.message);
          setStatus('error');
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const hostLink = user?.role === 'HOST' ? '/host/list' : '/signup?role=HOST';

  return (
    <div>
      <section className="grid items-center gap-10 lg:grid-cols-[1.15fr_0.85fr]">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.18em] text-gold">Delhi NCR · by the hour</p>
          <h1 className="mt-3 max-w-xl text-5xl leading-[1.05] text-forest-deep sm:text-6xl">
            A spare bay, ready when you are.
          </h1>
          <p className="mt-5 max-w-lg text-lg leading-8 text-muted">
            ParkDeck is a marketplace for unused parking. Hosts set an hourly price. Drivers book only the hours they need.
          </p>
          <div className="mt-8">
            <SearchBar
              onSearch={(filters) => {
                const params = new URLSearchParams();
                if (filters.city) params.set('city', filters.city);
                if (filters.keyword) params.set('keyword', filters.keyword);
                navigate(`/explore?${params.toString()}`);
              }}
            />
          </div>
        </div>
        <div className="relative">
          <img
            src="https://images.unsplash.com/photo-1506521781263-d8422e82f27a?auto=format&fit=crop&w=1400&q=80"
            alt="Covered parking bays"
            className="h-[420px] w-full rounded-[36px] object-cover"
          />
          <div className="absolute bottom-4 left-4 right-4 rounded-3xl bg-paper/95 p-4 shadow-lg">
            <p className="text-xs font-semibold uppercase tracking-wide text-gold">Tonight in Noida</p>
            <p className="mt-1 font-serif text-2xl">Covered slots from ₹40/hr</p>
          </div>
        </div>
      </section>

      <section className="mt-16">
        <div className="mb-6 flex items-end justify-between gap-4">
          <div>
            <p className="text-sm font-semibold uppercase tracking-wide text-gold">Featured</p>
            <h2 className="text-4xl">Spaces people can book</h2>
          </div>
          <Link to="/explore" className="text-sm font-semibold text-forest">Browse all</Link>
        </div>
        {status === 'error' && (
          <p className="rounded-2xl bg-paper px-4 py-3 text-sm text-red-800">{error}</p>
        )}
        {status === 'loading' && (
          <p className="text-muted">Loading featured spaces…</p>
        )}
        {status === 'ready' && featured.length === 0 && (
          <p className="text-muted">No active listings yet. A host can publish the first space.</p>
        )}
        <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
          {featured.map((space) => (
            <ParkingCard key={space.id} space={space} />
          ))}
        </div>
      </section>

      <section className="mt-16 grid gap-4 md:grid-cols-3">
        {STEPS.map((step) => (
          <article key={step.n} className="rounded-[28px] bg-paper p-6">
            <p className="text-sm font-semibold text-gold">{step.n}</p>
            <h3 className="mt-2 text-3xl">{step.title}</h3>
            <p className="mt-3 text-sm leading-6 text-muted">{step.body}</p>
          </article>
        ))}
      </section>

      <section className="mt-16 overflow-hidden rounded-[36px] bg-forest px-6 py-10 text-cream sm:px-10">
        <p className="text-sm font-semibold uppercase tracking-wide text-sand">For hosts</p>
        <h2 className="mt-2 max-w-lg text-4xl">An empty bay can take bookings while you are out.</h2>
        <p className="mt-3 max-w-xl text-cream/80">
          Set the hours, the hourly price in rupees, and a photo. You will see confirmed reservations and a total that excludes cancellations.
        </p>
        <Link to={hostLink} className="mt-6 inline-flex rounded-full bg-cream px-5 py-3 text-sm font-semibold text-forest">
          List your space
        </Link>
      </section>
    </div>
  );
}
