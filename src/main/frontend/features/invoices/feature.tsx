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

// One page of the all-invoices list as the /api/all-invoices endpoint serves it: the invoices on
// this page plus what the next/prev controls need (the 1-based page number and whether a page
// exists on either side).
type InvoicePage = {
  items: AllInvoice[];
  page: number;
  hasPrev: boolean;
  hasNext: boolean;
};

function InvoicesPage() {
  const [invoices, setInvoices] = useState<AllInvoice[]>([]);
  const [status, setStatus] = useState<string>('ALL');
  // The list is shown a page at a time (10 per page); this is the 1-based page currently on screen.
  const [page, setPage] = useState(1);
  const [hasPrev, setHasPrev] = useState(false);
  const [hasNext, setHasNext] = useState(false);

  async function load(current: string, currentPage: number) {
    const res = await fetch(
      `/api/all-invoices?status=${encodeURIComponent(current)}&page=${currentPage}`,
    );
    const data: InvoicePage = await res.json();
    setInvoices(data.items);
    setHasPrev(data.hasPrev);
    setHasNext(data.hasNext);
  }

  useEffect(() => {
    void load(status, page);
  }, [status, page]);

  // Changing the stage filter re-narrows the whole list, so go back to the first page.
  function changeStatus(next: string) {
    setStatus(next);
    setPage(1);
  }

  return (
    <section data-testid="invoices">
      <select
        data-testid="invoice-status-filter"
        value={status}
        onChange={(e) => changeStatus(e.target.value)}
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
      <div data-testid="invoice-pager">
        <button
          data-testid="invoice-page-prev"
          type="button"
          disabled={!hasPrev}
          onClick={() => setPage((p) => Math.max(1, p - 1))}
        >
          Previous
        </button>
        <span data-testid="invoice-page-label">{page}</span>
        <button
          data-testid="invoice-page-next"
          type="button"
          disabled={!hasNext}
          onClick={() => setPage((p) => p + 1)}
        >
          Next
        </button>
      </div>
    </section>
  );
}

export const feature: Feature = { id: 'invoices', label: 'Invoices', Page: InvoicesPage };
