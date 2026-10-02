// Shared money formatting primitive. Money is displayed everywhere with a currency symbol, grouped
// thousands and exactly two decimals, e.g. 100 -> "$100.00", 1000 -> "$1,000.00". Composed by
// features; not branched per-feature.
export function formatMoney(amount: number): string {
  return `$${Number(amount).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
}
