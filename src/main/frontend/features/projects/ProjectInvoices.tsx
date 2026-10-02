import React, { useEffect, useState } from 'react';
import { InvoiceLineItems } from './InvoiceLineItems';
import { InvoicePayments } from './InvoicePayments';

// A project's invoices, rendered inside the projects feature when a project is opened. Lists the
// project's invoices, shows their derived total (amounts to 2 decimals), and adds a new invoice by
// amount. Each invoice's status is worked out from the payments recorded against it — PARTIAL once
// some is paid, PAID once covered — rather than being flipped to paid by hand. Opening an invoice
// (invoice-open-<id>) shows its derived status, its line items — the things being charged for — and
// its payments in place of the list. Owns its own state and data loading (no global store);
// composed, not branched.
type Invoice = {
  id: number;
  projectId: number;
  amount: number;
  due: number;
  status: string;
  issuedDate: string | null;
  dueDate: string | null;
};

function formatAmount(amount: number): string {
  return `$${Number(amount).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
}

export function ProjectInvoices({ projectId }: { projectId: number }) {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [amount, setAmount] = useState('');
  const [error, setError] = useState('');
  const [sort, setSort] = useState<'id' | 'due'>('id');
  const [openInvoiceId, setOpenInvoiceId] = useState<number | null>(null);
  const [openStatus, setOpenStatus] = useState('');

  async function load(order: 'id' | 'due' = sort) {
    const res = await fetch(`/api/projects/${projectId}/invoices?sort=${order}`);
    setInvoices(await res.json());
  }

  // The opened invoice's status is derived from its payments, so re-read it on open and whenever a
  // payment is recorded against it.
  async function loadInvoice(invoiceId: number) {
    const res = await fetch(`/api/invoices/${invoiceId}`);
    if (!res.ok) {
      return;
    }
    const inv: Invoice = await res.json();
    setOpenStatus(inv.status);
  }

  useEffect(() => {
    void load();
  }, [projectId]);

  useEffect(() => {
    if (openInvoiceId !== null) {
      void loadInvoice(openInvoiceId);
    }
  }, [openInvoiceId]);

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

  const total = invoices.reduce((sum, inv) => sum + Number(inv.amount), 0);

  if (openInvoiceId !== null) {
    return (
      <section data-testid="project-invoices">
        <h2>Invoices</h2>
        <p>
          Status: <span data-testid="invoice-status">{openStatus}</span>
        </p>
        <InvoiceLineItems
          invoiceId={openInvoiceId}
          onClose={() => {
            setOpenInvoiceId(null);
            void load();
          }}
        />
        <InvoicePayments
          invoiceId={openInvoiceId}
          onRecorded={() => void loadInvoice(openInvoiceId)}
        />
      </section>
    );
  }

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
            <th>Due</th>
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
              <td data-testid="invoice-due-amount">{formatAmount(inv.due)}</td>
              <td data-testid="invoice-status">{inv.status}</td>
              <td data-testid="invoice-issued">{inv.issuedDate ?? ''}</td>
              <td data-testid="invoice-due">{inv.dueDate ?? ''}</td>
              <td>
                <button
                  type="button"
                  data-testid={`invoice-open-${inv.id}`}
                  onClick={() => setOpenInvoiceId(inv.id)}
                >
                  Open
                </button>
                {inv.status === 'DRAFT' ? (
                  <button
                    type="button"
                    data-testid={`invoice-send-${inv.id}`}
                    onClick={() => void send(inv.id)}
                  >
                    Send
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
