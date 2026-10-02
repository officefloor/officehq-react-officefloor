import { useEffect, useState } from 'react';
import { formatMoney } from '../../ui/money';

// One place listing every invoice across all projects. Each row shows the invoice's project NAME
// and its lifecycle stage (DRAFT -> SENT -> PAID). This feature owns its own state and data loading
// and does not import other features.
type AllInvoice = { id: number; projectName: string; amount: number; status: string };

export function AllInvoicesPage() {
  const [invoices, setInvoices] = useState<AllInvoice[]>([]);

  useEffect(() => {
    async function load() {
      const res = await fetch('/api/invoices');
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
            {invoices.map((invoice) => (
              <tr key={invoice.id} data-testid={`invoice-row-${invoice.id}`}>
                <td data-testid="invoice-project">{invoice.projectName}</td>
                <td data-testid="invoice-amount">{formatMoney(invoice.amount)}</td>
                <td data-testid="invoice-status">{invoice.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
