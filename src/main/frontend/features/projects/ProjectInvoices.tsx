import React, { useEffect, useState } from 'react';

// A project's invoices, shown when a project is opened from the projects list. Owns its own state
// (CLAUDE.md — features own their state). Lists the project's invoices, a derived total, and a form
// to add one. Amounts render with 2 decimals.
type Invoice = {
  id: number;
  projectId: number;
  amount: number;
  status: string;
  issuedDate: string | null;
  dueDate: string | null;
};

function money(n: number): string {
  return `$${Number(n).toFixed(2)}`;
}

export function ProjectInvoices({ projectId }: { projectId: number }) {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [amount, setAmount] = useState('');
  const [amountError, setAmountError] = useState('');
  const [sort, setSort] = useState('');

  async function load(sortBy = sort) {
    const query = sortBy ? `&sort=${encodeURIComponent(sortBy)}` : '';
    const res = await fetch(`/api/invoices?projectId=${projectId}${query}`);
    if (res.ok) {
      setInvoices(await res.json());
    }
  }

  useEffect(() => {
    void load();
  }, [projectId]);

  function sortByDue() {
    setSort('due');
    void load('due');
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    const parsed = Number(amount);
    if (!Number.isFinite(parsed) || parsed <= 0) {
      setAmountError('Amount must be more than zero.');
      return;
    }
    setAmountError('');
    const res = await fetch('/api/invoices', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ projectId, amount: Number(amount) }),
    });
    if (res.ok) {
      const created: Invoice = await res.json();
      setInvoices((prev) => [...prev, created]);
      setAmount('');
    }
  }

  async function payInvoice(id: number) {
    const res = await fetch('/api/invoices/pay', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id }),
    });
    if (res.ok) {
      const paid: Invoice = await res.json();
      setInvoices((prev) => prev.map((inv) => (inv.id === paid.id ? paid : inv)));
    }
  }

  const total = invoices.reduce((sum, inv) => sum + Number(inv.amount), 0);

  return (
    <section data-testid="project-invoices">
      <h2>Invoices</h2>
      <form data-testid="invoice-form" onSubmit={onSubmit}>
        <input
          data-testid="invoice-form-amount"
          placeholder="Amount"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
        />
        <button type="submit" data-testid="invoice-form-submit">
          Add invoice
        </button>
        {amountError ? (
          <p data-testid="invoice-form-amount-error" role="alert">
            {amountError}
          </p>
        ) : null}
      </form>

      <div data-testid="invoice-controls">
        <button type="button" data-testid="invoice-sort-due" onClick={sortByDue}>
          Sort by due date
        </button>
      </div>

      <table data-testid="project-invoices-table">
        <thead>
          <tr>
            <th>Amount</th>
            <th>Issued</th>
            <th>Due</th>
            <th>Status</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {invoices.map((inv) => (
            <tr key={inv.id} data-testid={`invoice-row-${inv.id}`}>
              <td data-testid="invoice-amount">{money(inv.amount)}</td>
              <td data-testid="invoice-issued">{inv.issuedDate ?? ''}</td>
              <td data-testid="invoice-due">{inv.dueDate ?? ''}</td>
              <td data-testid="invoice-status">{inv.status}</td>
              <td>
                {inv.status === 'PAID' ? null : (
                  <button
                    type="button"
                    data-testid={`invoice-pay-${inv.id}`}
                    onClick={() => void payInvoice(inv.id)}
                  >
                    Mark paid
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr>
            <td data-testid="project-invoices-total">{money(total)}</td>
          </tr>
        </tfoot>
      </table>
    </section>
  );
}
