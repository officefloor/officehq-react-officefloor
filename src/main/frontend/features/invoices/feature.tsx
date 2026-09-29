import React, { useEffect, useState } from 'react';
import type { Feature } from '../../router/routes';

// Invoices feature: one place listing EVERY invoice across all projects, showing which project each
// one is for and what stage it is at. Read-only; owns its own state and talks to its own
// /api/all-invoices endpoint. data-testid anchors follow the spec's conventions.
type AllInvoice = {
  id: number;
  projectId: number;
  projectName: string;
  amount: number;
  status: string;
};

function money(amount: number): string {
  return `$${Number(amount).toFixed(2)}`;
}

// The stages an invoice can be at; 'ALL' leaves the list unfiltered.
const STATUSES = ['DRAFT', 'SENT', 'PAID'] as const;

function InvoicesPage() {
  const [invoices, setInvoices] = useState<AllInvoice[]>([]);
  const [status, setStatus] = useState<string>('ALL');

  async function load(current: string) {
    const res = await fetch(`/api/all-invoices?status=${encodeURIComponent(current)}`);
    setInvoices(await res.json());
  }

  useEffect(() => {
    void load(status);
  }, [status]);

  return (
    <section data-testid="invoices">
      <select
        data-testid="invoice-status-filter"
        value={status}
        onChange={(e) => setStatus(e.target.value)}
      >
        <option value="ALL">All stages</option>
        {STATUSES.map((s) => (
          <option key={s} value={s}>
            {s}
          </option>
        ))}
      </select>
      {invoices.length === 0 ? (
        <p data-testid="all-invoices-empty">No invoices yet.</p>
      ) : (
        <table data-testid="all-invoices-table">
          <tbody>
            {invoices.map((inv) => (
              <tr key={inv.id} data-testid={`invoice-row-${inv.id}`}>
                <td data-testid="invoice-project">{inv.projectName}</td>
                <td data-testid="invoice-amount">{money(inv.amount)}</td>
                <td data-testid="invoice-status">{inv.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}

export const feature: Feature = { id: 'invoices', label: 'Invoices', Page: InvoicesPage };
