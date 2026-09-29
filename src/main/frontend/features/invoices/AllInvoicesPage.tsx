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

export function AllInvoicesPage() {
  const [invoices, setInvoices] = useState<InvoiceListing[]>([]);

  useEffect(() => {
    async function load() {
      const res = await fetch('/api/invoices/all');
      if (res.ok) {
        setInvoices(await res.json());
      }
    }
    void load();
  }, []);

  return (
    <section data-testid="all-invoices-page">
      <h1>Invoices</h1>
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
