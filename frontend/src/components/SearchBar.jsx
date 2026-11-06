import { useState } from 'react';
import { Search } from 'lucide-react';

const CITIES = ['', 'Greater Noida', 'Noida', 'Delhi'];
const TYPES = ['', 'COVERED', 'OPEN', 'BASEMENT', 'STREET'];

export default function SearchBar({ initial = {}, onSearch, showFilters = false }) {
  const [city, setCity] = useState(initial.city || '');
  const [keyword, setKeyword] = useState(initial.keyword || '');
  const [parkingType, setParkingType] = useState(initial.parkingType || '');
  const [maxPrice, setMaxPrice] = useState(initial.maxPrice || '');

  function submit(event) {
    event.preventDefault();
    onSearch({
      city,
      keyword,
      parkingType,
      maxPrice,
    });
  }

  return (
    <form onSubmit={submit} className="space-y-3 rounded-[28px] border border-line bg-paper p-3 shadow-sm">
      <div className="grid gap-3 md:grid-cols-2">
        <label className="block text-xs font-semibold uppercase tracking-wide text-muted">
          City
          <select
            value={city}
            onChange={(event) => setCity(event.target.value)}
            className="mt-1 w-full rounded-2xl border border-line px-3 py-3 text-sm text-ink"
          >
            {CITIES.map((item) => (
              <option key={item || 'all'} value={item}>{item || 'All NCR'}</option>
            ))}
          </select>
        </label>
        <label className="block text-xs font-semibold uppercase tracking-wide text-muted">
          Neighbourhood or keyword
          <input
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            placeholder="Pari Chowk, Saket, metro"
            className="mt-1 w-full rounded-2xl border border-line px-3 py-3 text-sm"
          />
        </label>
        {showFilters && (
          <>
            <label className="block text-xs font-semibold uppercase tracking-wide text-muted">
              Type
              <select
                value={parkingType}
                onChange={(event) => setParkingType(event.target.value)}
                className="mt-1 w-full rounded-2xl border border-line px-3 py-3 text-sm"
              >
                {TYPES.map((item) => (
                  <option key={item || 'any'} value={item}>{item || 'Any type'}</option>
                ))}
              </select>
            </label>
            <label className="block text-xs font-semibold uppercase tracking-wide text-muted">
              Max hourly price (INR)
              <input
                inputMode="decimal"
                value={maxPrice}
                onChange={(event) => setMaxPrice(event.target.value)}
                placeholder="80"
                className="mt-1 w-full rounded-2xl border border-line px-3 py-3 text-sm"
              />
            </label>
          </>
        )}
      </div>
      <button
        type="submit"
        className="inline-flex items-center justify-center gap-2 rounded-2xl bg-forest px-5 py-3 text-sm font-semibold text-cream"
      >
        <Search size={16} /> Search spaces
      </button>
    </form>
  );
}
