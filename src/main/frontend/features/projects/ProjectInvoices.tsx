import React, { useEffect, useState } from 'react';
import { InvoiceDetail } from './InvoiceDetail';

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
  due: number;
  currency: string;
};

// The client's money is shown in their own currency (e.g. USD -> "$100.00", EUR -> "€100.00").
function money(n: number, currency = 'USD'): string {
  return Number(n).toLocaleString('en-US', { style: 'currency', currency });
}

export function ProjectInvoices({
  projectId,
  onOpenInvoiceChange,
}: {
  projectId: number;
  onOpenInvoiceChange?: (invoiceId: number | null) => void;
}) {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [amount, setAmount] = useState('');
  const [amountError, setAmountError] = useState('');
  const [sort, setSort] = useState('');
  const [openInvoiceId, setOpenInvoiceId] = useState<number | null>(null);

  // Drilling into an invoice's detail is a focused view: let the page hide the other project-level
  // sections (each of which owns its own state) while an invoice is open, and restore them on close.
  function openInvoice(id: number | null) {
    setOpenInvoiceId(id);
    onOpenInvoiceChange?.(id);
  }

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

  async function sendInvoice(id: number) {
    const res = await fetch('/api/invoices/send', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id }),
    });
    if (res.ok) {
      const sent: Invoice = await res.json();
      setInvoices((prev) => prev.map((inv) => (inv.id === sent.id ? sent : inv)));
    }
  }

  // Cancel (void) an invoice sent by mistake: it reads VOID and stops counting toward money owed.
  async function cancelInvoice(id: number) {
    const res = await fetch('/api/invoices/cancel', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id }),
    });
    if (res.ok) {
      const voided: Invoice = await res.json();
      setInvoices((prev) => prev.map((inv) => (inv.id === voided.id ? voided : inv)));
    }
  }

  const total = invoices.reduce((sum, inv) => sum + Number(inv.amount), 0);
  // All of a project's invoices belong to one client, so they share a currency; use the first row's
  // (falling back to USD for an empty list).
  const currency = invoices[0]?.currency ?? 'USD';

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

      {openInvoiceId !== null ? (
        <>
          <button
            type="button"
            data-testid="invoice-close"
            onClick={() => openInvoice(null)}
          >
            Back to invoices
          </button>
          <InvoiceDetail invoiceId={openInvoiceId} />
        </>
      ) : (
        <>
      <div data-testid="invoice-controls">
        <button type="button" data-testid="invoice-sort-due" onClick={sortByDue}>
          Sort by due date
        </button>
      </div>

      <table data-testid="project-invoices-table">
        <thead>
          <tr>
            <th>Amount</th>
            <th>Still due</th>
            <th>Issued</th>
            <th>Due</th>
            <th>Status</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {invoices.map((inv) => (
            <tr key={inv.id} data-testid={`invoice-row-${inv.id}`}>
              <td data-testid="invoice-amount">{money(inv.amount, inv.currency)}</td>
              <td data-testid="invoice-due-amount">{money(inv.due, inv.currency)}</td>
              <td data-testid="invoice-issued">{inv.issuedDate ?? ''}</td>
              <td data-testid="invoice-due">{inv.dueDate ?? ''}</td>
              <td data-testid="invoice-status">{inv.status}</td>
              <td>
                <button
                  type="button"
                  data-testid={`invoice-open-${inv.id}`}
                  onClick={() => openInvoice(inv.id)}
                >
                  Open
                </button>
                {inv.status === 'DRAFT' ? (
                  <button
                    type="button"
                    data-testid={`invoice-send-${inv.id}`}
                    onClick={() => void sendInvoice(inv.id)}
                  >
                    Send
                  </button>
                ) : null}
                {inv.status === 'SENT' ? (
                  <button
                    type="button"
                    data-testid={`invoice-cancel-${inv.id}`}
                    onClick={() => void cancelInvoice(inv.id)}
                  >
                    Cancel
                  </button>
                ) : null}
              </td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr>
            <td data-testid="project-invoices-total">{money(total, currency)}</td>
          </tr>
        </tfoot>
      </table>
        </>
      )}
    </section>
  );
}
