import { useEffect, useState } from 'react';
import { formatMoney } from '../../ui/money';

// One place listing every invoice across all projects. Each row shows the invoice's project NAME
// and its lifecycle stage (DRAFT -> SENT -> PAID). This feature owns its own state and data loading
// and does not import other features.
type AllInvoice = { id: number; projectName: string; amount: number; status: string };

// Lifecycle stages an invoice can be narrowed to; empty value means "all stages".
const STAGES = ['DRAFT', 'SENT', 'PAID'];

export function AllInvoicesPage() {
  const [invoices, setInvoices] = useState<AllInvoice[]>([]);
  const [status, setStatus] = useState('');

  useEffect(() => {
    async function load() {
      const query = status ? `?status=${encodeURIComponent(status)}` : '';
      const res = await fetch(`/api/invoices${query}`);
      if (res.ok) {
        setInvoices(await res.json());
      }
    }
    void load();
  }, [status]);

  return (
    <section data-testid="all-invoices-page">
      <h1>Invoices</h1>

      <label>
        Stage
        <select
          data-testid="invoice-status-filter"
          value={status}
          onChange={(e) => setStatus(e.target.value)}
        >
          <option value="">All stages</option>
          {STAGES.map((stage) => (
            <option key={stage} value={stage}>
              {stage}
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
