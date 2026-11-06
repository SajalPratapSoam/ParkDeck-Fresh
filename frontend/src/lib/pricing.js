function divideRound(numerator, denominator, scale) {
  const factor = 10n ** BigInt(scale);
  const numeratorScaled = BigInt(numerator) * factor;
  const divisor = BigInt(denominator);
  const quotient = numeratorScaled / divisor;
  const remainder = numeratorScaled % divisor;
  const roundUp = remainder * 2n >= divisor ? 1n : 0n;
  return quotient + roundUp;
}

function toScale(value, scale) {
  const [whole, fraction = ''] = String(value).split('.');
  const digits = (fraction + '0'.repeat(scale)).slice(0, scale);
  return BigInt(`${whole}${digits}`);
}

function fromPaise(paise) {
  const negative = paise < 0n;
  const absolute = negative ? -paise : paise;
  const text = absolute.toString().padStart(3, '0');
  const body = `${text.slice(0, -2)}.${text.slice(-2)}`;
  return negative ? `-${body}` : body;
}

export function quote(hourlyPrice, startValue, endValue) {
  const start = new Date(startValue);
  const end = new Date(endValue);
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) {
    return null;
  }
  const minutes = Math.round((end.getTime() - start.getTime()) / 60000);
  if (minutes <= 0) {
    return null;
  }
  const hours = divideRound(minutes, 60, 4);
  const rate = toScale(hourlyPrice, 2);
  const subtotal = divideRound(rate * hours, 10000n, 0);
  const serviceFee = divideRound(subtotal * 5n, 100n, 0);
  return {
    minutes,
    subtotal: fromPaise(subtotal),
    serviceFee: fromPaise(serviceFee),
    total: fromPaise(subtotal + serviceFee),
  };
}
