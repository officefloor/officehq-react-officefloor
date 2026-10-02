import React, { useEffect, useState } from 'react';

// A project's invoices, rendered inside the projects feature when a project is opened. Lists the
// project's invoices, shows their derived total (amounts to 2 decimals), and adds a new invoice by
// amount. Owns its own state and data loading (no global store); composed, not branched.
type Invoice = {
  id: number;
  projectId: number;
  amount: number;
  status: string;
  issuedDate: string | null;
  dueDate: string | null;
};

function formatAmount(amount: number): string {
  return `$${Number(amount).toFixed(2)}`;
}

export function ProjectInvoices({ projectId }: { projectId: number }) {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [amount, setAmount] = useState('');
  const [error, setError] = useState('');
  const [sort, setSort] = useState<'id' | 'due'>('id');

  async function load(order: 'id' | 'due' = sort) {
    const res = await fetch(`/api/projects/${projectId}/invoices?sort=${order}`);
    setInvoices(await res.json());
  }

  useEffect(() => {
    void load();
  }, [projectId]);

  async function sortByDue() {
    setSort('due');
    await load('due');
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    const value = Number(amount);
    if (!(value > 0)) {
      setError('Amount must be greater than zero');
      return;
    }
    setError('');
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

  async function send(invoiceId: number) {
    const res = await fetch(`/api/invoices/${invoiceId}/send`, { method: 'POST' });
    if (!res.ok) {
      return;
    }
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
        {error ? (
          <p data-testid="invoice-form-amount-error" role="alert">
            {error}
          </p>
        ) : null}
      </form>

      <button type="button" data-testid="invoice-sort-due" onClick={() => void sortByDue()}>
        Sort by due date
      </button>

      <table data-testid="project-invoices-table">
        <thead>
          <tr>
            <th>Amount</th>
            <th>Status</th>
            <th>Issued</th>
            <th>Due</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {invoices.map((inv) => (
            <tr key={inv.id} data-testid={`invoice-row-${inv.id}`}>
              <td data-testid="invoice-amount">{formatAmount(inv.amount)}</td>
              <td data-testid="invoice-status">{inv.status}</td>
              <td data-testid="invoice-issued">{inv.issuedDate ?? ''}</td>
              <td data-testid="invoice-due">{inv.dueDate ?? ''}</td>
              <td>
                {inv.status === 'DRAFT' ? (
                  <button
                    type="button"
                    data-testid={`invoice-send-${inv.id}`}
                    onClick={() => void send(inv.id)}
                  >
                    Send
                  </button>
                ) : null}
                {inv.status === 'SENT' ? (
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
