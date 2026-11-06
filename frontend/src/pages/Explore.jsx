import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { api, ApiError, toQuery } from '../api/client';
import ParkingCard from '../components/ParkingCard';
import SearchBar from '../components/SearchBar';
import StateMessage from '../components/StateMessage';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

export default function Explore() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const notify = useToast();
  const filters = {
    city: params.get('city') || '',
    keyword: params.get('keyword') || '',
    parkingType: params.get('parkingType') || '',
    maxPrice: params.get('maxPrice') || '',
  };
  const [page, setPage] = useState(null);
  const [savedIds, setSavedIds] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');
    api(`/api/spaces${toQuery({ ...filters, size: 12 })}`, { auth: false })
      .then((data) => {
        if (!cancelled) setPage(data);
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
  }, [filters.city, filters.keyword, filters.parkingType, filters.maxPrice]);

  useEffect(() => {
    if (!user) {
      setSavedIds([]);
      return;
    }
    api('/api/wishlist')
      .then((items) => setSavedIds(items.map((item) => item.id)))
      .catch(() => setSavedIds([]));
  }, [user]);

  async function toggleSave(space) {
    if (!user) {
      navigate('/login', { state: { from: '/explore' } });
      return;
    }
    try {
      if (savedIds.includes(space.id)) {
        await api(`/api/wishlist/${space.id}`, { method: 'DELETE' });
        setSavedIds((current) => current.filter((id) => id !== space.id));
        notify('Removed from your wishlist.');
      } else {
        await api(`/api/wishlist/${space.id}`, { method: 'POST' });
        setSavedIds((current) => [...current, space.id]);
        notify('Saved to your wishlist.');
      }
    } catch (err) {
      notify(err instanceof ApiError ? err.message : 'Could not update the wishlist.');
    }
  }

  return (
    <div>
      <p className="text-sm font-semibold uppercase tracking-wide text-gold">Explore</p>
      <h1 className="mt-1 text-5xl">Parking across Delhi NCR</h1>
      <div className="mt-6">
        <SearchBar
          key={params.toString()}
          showFilters
          initial={filters}
          onSearch={(next) => {
            const search = new URLSearchParams();
            Object.entries(next).forEach(([key, value]) => {
              if (value) search.set(key, value);
            });
            navigate(`/explore?${search.toString()}`);
          }}
        />
      </div>
      <div className="mt-8">
        {loading && <StateMessage loading title="Looking up spaces" />}
        {!loading && error && <StateMessage title="Could not load parking" body={error} />}
        {!loading && !error && page?.content?.length === 0 && (
          <StateMessage
            title="No spaces match"
            body="Try another city, a higher hourly budget, or clear the keyword."
            action={{ to: '/explore', label: 'Clear filters' }}
          />
        )}
        {!loading && !error && page?.content?.length > 0 && (
          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
            {page.content.map((space) => (
              <ParkingCard
                key={space.id}
                space={space}
                saved={savedIds.includes(space.id)}
                onToggleSave={toggleSave}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
