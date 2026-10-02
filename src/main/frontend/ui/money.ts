// Shared money formatting primitive. Money is displayed everywhere with a currency symbol, grouped
// thousands and exactly two decimals, e.g. 100 -> "$100.00", 1000 -> "$1,000.00". Different clients
// are paid in different currencies, so the symbol is chosen from the currency; it defaults to USD
// when none is given. Composed by features; not branched per-feature.
const CURRENCY_SYMBOLS: Record<string, string> = {
  USD: '$',
  EUR: '€',
};

export function formatMoney(amount: number, currency: string = 'USD'): string {
  const symbol = CURRENCY_SYMBOLS[currency] ?? '$';
  return `${symbol}${Number(amount).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
}
