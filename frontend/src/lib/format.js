export function inr(value) {
  const amount = Number(value);
  if (!Number.isFinite(amount)) {
    return '₹0';
  }
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(amount);
}

export function when(value) {
  if (!value) {
    return '';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return new Intl.DateTimeFormat('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date);
}

export function hoursLabel(minutes) {
  const hrs = Math.floor(minutes / 60);
  const mins = minutes % 60;
  if (hrs && mins) {
    return `${hrs} hr ${mins} min`;
  }
  if (hrs) {
    return `${hrs} hr`;
  }
  return `${mins} min`;
}

const TYPE_LABELS = {
  COVERED: 'Covered',
  OPEN: 'Open',
  BASEMENT: 'Basement',
  STREET: 'Street',
};

export function typeLabel(type) {
  return TYPE_LABELS[type] || type;
}

export function clock(value) {
  if (!value) {
    return '';
  }
  return String(value).slice(0, 5);
}
