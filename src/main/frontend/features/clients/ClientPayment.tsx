import React, { useEffect, useState } from 'react';

// Record a single lump sum a client paid and split it across several of their open invoices, rendered
// inside the clients feature when a client is opened. Opening the form (client-record-payment) reads
// the client's statement for the invoices that still owe something, then takes the lump amount, the
// date, and a per-invoice share (payment-alloc-<invoiceId>). Submitting posts the lump and its
// allocations; the server settles each invoice from its share so the balances come out right. Owns its
// own state and data loading (no global store); composed, not branched.
type StatementInvoice = {
  id: number;
  projectId: number;
  amount: number;
  due: number;
  status: string;
};

export function ClientPayment({ clientId }: { clientId: number }) {
  const [open, setOpen] = useState(false);
  const [invoices, setInvoices] = useState<StatementInvoice[]>([]);
  const [amount, setAmount] = useState('');
  const [date, setDate] = useState('');
  // The share entered against each invoice, keyed by invoice id (blank = nothing allocated there).
  const [allocs, setAllocs] = useState<Record<number, string>>({});
  const [error, setError] = useState('');

  // Collapse and reset the form when the opened client changes, so it opens fresh per client.
  useEffect(() => {
    setOpen(false);
    setInvoices([]);
    setAmount('');
    setDate('');
    setAllocs({});
    setError('');
  }, [clientId]);

  async function openForm() {
    const res = await fetch(`/api/clients/${clientId}/statement`);
    if (!res.ok) {
      return;
    }
    const statement: { invoices: StatementInvoice[] } = await res.json();
    // Only invoices that still owe something can take a share of the payment.
    setInvoices(statement.invoices.filter((inv) => inv.status !== 'VOID' && Number(inv.due) > 0));
    setOpen(true);
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    const total = Number(amount);
    if (!(total > 0) || !date.trim()) {
      setError('An amount greater than zero and a date are required');
      return;
    }
    const allocations = invoices
      .map((inv) => ({ invoiceId: inv.id, amount: Number(allocs[inv.id] ?? '') }))
      .filter((a) => a.amount > 0);
    if (allocations.length === 0) {
      setError('Allocate the payment across at least one invoice');
      return;
    }
    const allocated = allocations.reduce((sum, a) => sum + a.amount, 0);
    if (Math.abs(allocated - total) > 0.001) {
      setError('The allocations must add up to the payment amount');
      return;
    }
    setError('');
    const res = await fetch(`/api/clients/${clientId}/payments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ amount: total, date, allocations }),
    });
    if (!res.ok) {
      setError('The payment could not be recorded');
      return;
    }
    setOpen(false);
    setAmount('');
    setDate('');
    setAllocs({});
  }

  return (
    <section data-testid="client-payment">
      <button type="button" data-testid="client-record-payment" onClick={() => void openForm()}>
        Record payment
      </button>

      {open ? (
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

          <table data-testid="payment-alloc-table">
            <thead>
              <tr>
                <th>Invoice</th>
                <th>Due</th>
                <th>Allocate</th>
              </tr>
            </thead>
            <tbody>
              {invoices.map((inv) => (
                <tr key={inv.id} data-testid={`payment-alloc-row-${inv.id}`}>
                  <td>{inv.id}</td>
                  <td>{inv.due}</td>
                  <td>
                    <input
                      data-testid={`payment-alloc-${inv.id}`}
                      placeholder="0"
                      value={allocs[inv.id] ?? ''}
                      onChange={(e) =>
                        setAllocs((prev) => ({ ...prev, [inv.id]: e.target.value }))
                      }
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <button type="submit" data-testid="payment-form-submit">
            Record payment
          </button>
          {error ? (
            <p data-testid="payment-form-error" role="alert">
              {error}
            </p>
          ) : null}
        </form>
      ) : null}
    </section>
  );
}
