import React, { useState } from 'react';

// Record one lump payment from a client and split it across their open invoices. Owns its own state
// (CLAUDE.md — features own their state; clients does not import the invoices feature). It reads the
// client's open invoices from the same GET /api/clients/statement endpoint the statement uses, then
// posts the split to POST /api/payments/split, which records one payment per allocated invoice so each
// invoice's derived balance settles on its own.
type StatementInvoice = {
  id: number;
  projectName: string;
  due: number;
  status: string;
};

function money(n: number): string {
  return Number(n).toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}

export function ClientRecordPayment({ clientId }: { clientId: number }) {
  const [open, setOpen] = useState(false);
  const [invoices, setInvoices] = useState<StatementInvoice[]>([]);
  const [amount, setAmount] = useState('');
  const [date, setDate] = useState('');
  const [allocations, setAllocations] = useState<Record<number, string>>({});

  async function begin() {
    setOpen(true);
    const res = await fetch(`/api/clients/statement?clientId=${clientId}`);
    if (res.ok) {
      const statement: { invoices: StatementInvoice[] } = await res.json();
      // Only invoices that still owe something can take a share of the payment.
      setInvoices(statement.invoices.filter((inv) => Number(inv.due) > 0));
    }
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    const shares = invoices
      .map((inv) => ({ invoiceId: inv.id, amount: Number(allocations[inv.id]) }))
      .filter((a) => Number.isFinite(a.amount) && a.amount > 0);
    const res = await fetch('/api/payments/split', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ amount: Number(amount), date, allocations: shares }),
    });
    if (res.ok) {
      setOpen(false);
      setAmount('');
      setDate('');
      setAllocations({});
    }
  }

  return (
    <section data-testid="client-payment">
      <button type="button" data-testid="client-record-payment" onClick={() => void begin()}>
        Record payment
      </button>
      {open ? (
        <form data-testid="payment-form" onSubmit={onSubmit}>
          <label>
            Amount
            <input
              data-testid="payment-form-amount"
              placeholder="Amount"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
            />
          </label>
          <label>
            Date
            <input
              data-testid="payment-form-date"
              placeholder="yyyy-mm-dd"
              value={date}
              onChange={(e) => setDate(e.target.value)}
            />
          </label>
          <table data-testid="payment-alloc-table">
            <thead>
              <tr>
                <th>Job</th>
                <th>Due</th>
                <th>Apply</th>
              </tr>
            </thead>
            <tbody>
              {invoices.map((inv) => (
                <tr key={inv.id} data-testid={`payment-alloc-row-${inv.id}`}>
                  <td>{inv.projectName}</td>
                  <td>{money(inv.due)}</td>
                  <td>
                    <input
                      data-testid={`payment-alloc-${inv.id}`}
                      placeholder="0"
                      value={allocations[inv.id] ?? ''}
                      onChange={(e) =>
                        setAllocations((prev) => ({ ...prev, [inv.id]: e.target.value }))
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
        </form>
      ) : null}
    </section>
  );
}
