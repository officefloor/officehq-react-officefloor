import React, { useEffect, useState } from 'react';

// Invoices feature: one place listing every invoice across all projects, showing which project each
// is for and what stage (status) it is at. Owns its own state (CLAUDE.md — features own their
// state, no global store). The project name is joined server-side (GET /api/invoices/all).
type InvoiceListing = {
  id: number;
  projectId: number;
  projectName: string;
  amount: number;
  status: string;
};

function money(n: number): string {
  return `$${Number(n).toFixed(2)}`;
}

// The lifecycle stages an invoice can sit at (see V5/V8 migrations: DRAFT -> SENT -> PAID). The
// filter narrows the all-invoices list to one stage; the empty value keeps every stage visible.
const STAGES = ['DRAFT', 'SENT', 'PAID'];

export function AllInvoicesPage() {
  const [invoices, setInvoices] = useState<InvoiceListing[]>([]);
  const [stage, setStage] = useState<string>('');

  useEffect(() => {
    async function load() {
      const res = await fetch('/api/invoices/all');
      if (res.ok) {
        setInvoices(await res.json());
      }
    }
    void load();
  }, []);

  const shown = stage ? invoices.filter((inv) => inv.status === stage) : invoices;

  return (
    <section data-testid="all-invoices-page">
      <h1>Invoices</h1>
      <label>
        Stage
        <select
          data-testid="invoice-status-filter"
          value={stage}
          onChange={(e) => setStage(e.target.value)}
        >
          <option value="">All stages</option>
          {STAGES.map((s) => (
            <option key={s} value={s}>
              {s}
            </option>
          ))}
        </select>
      </label>
      {invoices.length === 0 ? (
        <p data-testid="all-invoices-empty">No invoices yet.</p>
      ) : (
        <table data-testid="all-invoices-table">
          <thead>
            <tr>
              <th>Project</th>
              <th>Amount</th>
              <th>Stage</th>
            </tr>
          </thead>
          <tbody>
            {shown.map((inv) => (
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
