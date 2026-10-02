import React, { useEffect, useState } from 'react';

// The invoices feature owns its own state and data loading (no global store). One place that lists
// every invoice across all projects, showing which project each one is for (the project's NAME, a
// cross-entity join surfaced in the UI) and the stage (status) it is at. Served by /api/invoices.
type AllInvoice = {
  id: number;
  projectId: number;
  projectName: string;
  amount: number;
  status: string;
};

function formatAmount(amount: number): string {
  return `$${Number(amount).toFixed(2)}`;
}

export function AllInvoicesPage() {
  const [invoices, setInvoices] = useState<AllInvoice[]>([]);

  useEffect(() => {
    async function load() {
      const res = await fetch('/api/invoices');
      setInvoices(await res.json());
    }
    void load();
  }, []);

  return (
    <section data-testid="invoices-section">
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
                <td data-testid="invoice-amount">{formatAmount(inv.amount)}</td>
                <td data-testid="invoice-status">{inv.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
