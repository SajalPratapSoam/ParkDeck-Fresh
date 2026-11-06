import { useEffect, useState } from 'react';
import { api } from '../api/client';
import ParkingCard from '../components/ParkingCard';
import StateMessage from '../components/StateMessage';
import { useToast } from '../context/ToastContext';

export default function Wishlist() {
  const notify = useToast();
  const [spaces, setSpaces] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api('/api/wishlist')
      .then(setSpaces)
      .catch((err) => setError(err.message));
  }, []);

  async function remove(space) {
    try {
      await api(`/api/wishlist/${space.id}`, { method: 'DELETE' });
      setSpaces((current) => current.filter((item) => item.id !== space.id));
      notify('Removed from your wishlist.');
    } catch (err) {
      notify(err.message);
    }
  }

  if (error) return <StateMessage title="Could not load your wishlist" body={error} />;
  if (!spaces) return <StateMessage loading title="Loading saved spaces" />;
  if (spaces.length === 0) {
    return (
      <StateMessage
        title="Nothing saved yet"
        body="Tap the heart on a parking card to keep it here."
        action={{ to: '/explore', label: 'Explore parking' }}
      />
    );
  }

  return (
    <div>
      <h1 className="text-5xl">Wishlist</h1>
      <div className="mt-6 grid gap-5 md:grid-cols-2 lg:grid-cols-3">
        {spaces.map((space) => (
          <ParkingCard key={space.id} space={space} saved onToggleSave={remove} />
        ))}
      </div>
    </div>
  );
}
