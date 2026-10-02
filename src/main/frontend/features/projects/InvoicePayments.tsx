import React, { useEffect, useState } from 'react';

// An opened invoice's payments, rendered inside the projects feature alongside its line items. The
// owner records what a client has actually paid against the invoice — an amount and the date it was
// paid — and the payments are listed back. Owns its own state and data loading (no global store);
// composed, not branched.
type Payment = {
  id: number;
  invoiceId: number;
  amount: number;
  date: string;
};

function formatAmount(amount: number): string {
  return `$${Number(amount).toFixed(2)}`;
}

export function InvoicePayments({ invoiceId }: { invoiceId: number }) {
  const [payments, setPayments] = useState<Payment[]>([]);
  const [amount, setAmount] = useState('');
  const [date, setDate] = useState('');
  const [error, setError] = useState('');

  async function load() {
    const res = await fetch(`/api/invoices/${invoiceId}/payments`);
    setPayments(await res.json());
  }

  useEffect(() => {
    void load();
  }, [invoiceId]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    const value = Number(amount);
    if (!(value > 0) || !date.trim()) {
      setError('An amount greater than zero and a date are required');
      return;
    }
    setError('');
    const res = await fetch(`/api/invoices/${invoiceId}/payments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ amount: value, date }),
    });
    if (!res.ok) {
      return;
    }
    setAmount('');
    setDate('');
    await load();
  }

  return (
    <section data-testid="invoice-payments">
      <h2>Payments</h2>

      <form data-testid="payment-form" onSubmit={onSubmit}>
        <input
          data-testid="payment-form-amount"
          placeholder="Amount"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
        />
        <input
          data-testid="payment-form-date"
          placeholder="Date"
          value={date}
          onChange={(e) => setDate(e.target.value)}
        />
        <button type="submit" data-testid="payment-form-submit">
          Record payment
        </button>
        {error ? (
          <p data-testid="payment-form-error" role="alert">
            {error}
          </p>
        ) : null}
      </form>

      <table data-testid="invoice-payments-table">
        <thead>
          <tr>
            <th>Amount</th>
            <th>Date</th>
          </tr>
        </thead>
        <tbody>
          {payments.map((p) => (
            <tr key={p.id} data-testid={`payment-row-${p.id}`}>
              <td data-testid="payment-amount">{formatAmount(p.amount)}</td>
              <td data-testid="payment-date">{p.date}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
