import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Heart, MapPin } from 'lucide-react';
import { clock, inr, typeLabel } from '../lib/format';

export default function ParkingCard({ space, saved = false, onToggleSave }) {
  const [imageFailed, setImageFailed] = useState(false);

  return (
    <article className="flex h-full flex-col overflow-hidden rounded-[28px] border border-line bg-paper shadow-[0_16px_40px_rgba(28,25,23,0.05)]">
      <Link to={`/spaces/${space.id}`} className="relative block">
        {imageFailed ? (
          <div className="flex h-52 items-end bg-forest p-4 text-cream">Photo unavailable</div>
        ) : (
          <img
            src={space.imageUrl}
            alt=""
            className="h-52 w-full object-cover"
            onError={() => setImageFailed(true)}
          />
        )}
        <span className="absolute left-3 top-3 rounded-full bg-paper/95 px-3 py-1 text-xs font-semibold">
          {typeLabel(space.parkingType)}
        </span>
      </Link>
      <div className="flex flex-1 flex-col p-4">
        <div className="flex items-start justify-between gap-3">
          <div>
            <h3 className="text-2xl leading-tight">
              <Link to={`/spaces/${space.id}`}>{space.title}</Link>
            </h3>
            <p className="mt-1 flex items-center gap-1 text-sm text-muted">
              <MapPin size={14} /> {space.city}
            </p>
          </div>
          {onToggleSave && (
            <button
              type="button"
              aria-label={saved ? 'Remove from wishlist' : 'Save to wishlist'}
              onClick={() => onToggleSave(space)}
              className={`rounded-full border p-2 ${saved ? 'border-forest bg-forest text-cream' : 'border-line text-forest'}`}
            >
              <Heart size={16} fill={saved ? 'currentColor' : 'none'} />
            </button>
          )}
        </div>
        <p className="mt-3 line-clamp-2 text-sm leading-6 text-muted">{space.address}</p>
        <div className="mt-auto flex items-end justify-between pt-4">
          <p className="text-sm text-muted">
            {clock(space.availableFrom)}–{clock(space.availableUntil)}
          </p>
          <p className="text-lg font-semibold text-forest">
            {inr(space.hourlyPrice)}
            <span className="text-sm font-medium text-muted"> /hr</span>
          </p>
        </div>
      </div>
    </article>
  );
}
