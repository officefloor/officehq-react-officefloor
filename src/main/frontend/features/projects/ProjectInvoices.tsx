import React, { useEffect, useState } from 'react';

// A project's invoices, rendered inside the projects feature when a project is opened. Lists the
// project's invoices, shows their derived total (amounts to 2 decimals), and adds a new invoice by
// amount. Owns its own state and data loading (no global store); composed, not branched.
type Invoice = { id: number; projectId: number; amount: number; status: string };

function formatAmount(amount: number): string {
  return `$${Number(amount).toFixed(2)}`;
}

export function ProjectInvoices({ projectId }: { projectId: number }) {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [amount, setAmount] = useState('');

  async function load() {
    const res = await fetch(`/api/projects/${projectId}/invoices`);
    setInvoices(await res.json());
  }

  useEffect(() => {
    void load();
  }, [projectId]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    const res = await fetch(`/api/projects/${projectId}/invoices`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ amount: Number(amount) }),
    });
    if (!res.ok) {
      return;
    }
    setAmount('');
    await load();
  }

  async function pay(invoiceId: number) {
    const res = await fetch(`/api/invoices/${invoiceId}/pay`, { method: 'POST' });
    if (!res.ok) {
      return;
    }
    await load();
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
      </form>

      <table data-testid="project-invoices-table">
        <thead>
          <tr>
            <th>Amount</th>
            <th>Status</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {invoices.map((inv) => (
            <tr key={inv.id} data-testid={`invoice-row-${inv.id}`}>
              <td data-testid="invoice-amount">{formatAmount(inv.amount)}</td>
              <td data-testid="invoice-status">{inv.status}</td>
              <td>
                {inv.status === 'UNPAID' ? (
                  <button
                    type="button"
                    data-testid={`invoice-pay-${inv.id}`}
                    onClick={() => void pay(inv.id)}
                  >
                    Mark paid
                  </button>
                ) : null}
              </td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr>
            <td data-testid="project-invoices-total">{formatAmount(total)}</td>
          </tr>
        </tfoot>
      </table>
    </section>
  );
}
