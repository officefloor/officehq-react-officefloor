import { useEffect, useState } from 'react';
import { formatMoney } from '../../ui/money';

// One place listing every invoice across all projects. Each row shows the invoice's project NAME
// and its lifecycle stage (DRAFT -> SENT -> PAID). The list is large, so it is shown a page at a
// time (ten per page) with next/previous controls. This feature owns its own state and data
// loading and does not import other features.
type AllInvoice = { id: number; projectName: string; amount: number; status: string };
type InvoicePage = { items: AllInvoice[]; page: number; pageCount: number; total: number };

// Lifecycle stages an invoice can be narrowed to; empty value means "all stages".
const STAGES = ['DRAFT', 'SENT', 'PAID'];
const PAGE_SIZE = 10;

export function AllInvoicesPage() {
  const [invoices, setInvoices] = useState<AllInvoice[]>([]);
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(1);
  const [pageCount, setPageCount] = useState(1);

  useEffect(() => {
    async function load() {
      const params = new URLSearchParams({ page: String(page), size: String(PAGE_SIZE) });
      if (status) {
        params.set('status', status);
      }
      const res = await fetch(`/api/invoices?${params.toString()}`);
      if (res.ok) {
        const body: InvoicePage = await res.json();
        setInvoices(body.items);
        setPageCount(body.pageCount);
      }
    }
    void load();
  }, [status, page]);

  // Changing the stage filter re-scopes the list, so start again at the first page.
  function changeStatus(next: string) {
    setStatus(next);
    setPage(1);
  }

  return (
    <section data-testid="all-invoices-page">
      <h1>Invoices</h1>

      <label>
        Stage
        <select
          data-testid="invoice-status-filter"
          value={status}
          onChange={(e) => changeStatus(e.target.value)}
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
        <>
          <table data-testid="all-invoices-table">
            <thead>
              <tr>
                <th>Job</th>
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

          <nav data-testid="invoice-pagination">
            <button
              type="button"
              data-testid="invoice-page-prev"
              disabled={page <= 1}
              onClick={() => setPage((p) => Math.max(1, p - 1))}
            >
              Previous
            </button>
            <span data-testid="invoice-page-label">{page}</span>
            <button
              type="button"
              data-testid="invoice-page-next"
              disabled={page >= pageCount}
              onClick={() => setPage((p) => p + 1)}
            >
              Next
            </button>
          </nav>
        </>
      )}
    </section>
  );
}
