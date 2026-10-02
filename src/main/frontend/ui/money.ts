// Shared money formatting primitive. Money is displayed everywhere with a currency symbol and
// exactly two decimals, e.g. 100 -> "$100.00". Composed by features; not branched per-feature.
export function formatMoney(amount: number): string {
  return `$${Number(amount).toFixed(2)}`;
}
