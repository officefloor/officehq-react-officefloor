import React, { useEffect, useState } from 'react';

// Invoices feature: one place listing every invoice across all projects, showing which project each
// is for and what stage (status) it is at. Owns its own state (CLAUDE.md — features own their
// state, no global store). The list has grown large, so it is shown a PAGE AT A TIME (10 per page)
// with next/previous controls; the page slice, project-name join and stage filter are done
// server-side (GET /api/invoices/all?page=&status=).
type InvoiceListing = {
  id: number;
  projectId: number;
  projectName: string;
  amount: number;
  status: string;
};

type InvoicePage = {
  invoices: InvoiceListing[];
  total: number;
  page: number;
  pageSize: number;
};

function money(n: number): string {
  return `$${Number(n).toFixed(2)}`;
}

// The lifecycle stages an invoice can sit at (see V5/V8 migrations: DRAFT -> SENT -> PAID). The
// filter narrows the all-invoices list to one stage; the empty value keeps every stage visible.
const STAGES = ['DRAFT', 'SENT', 'PAID'];

export function AllInvoicesPage() {
  const [invoices, setInvoices] = useState<InvoiceListing[]>([]);
  const [total, setTotal] = useState<number>(0);
  const [pageSize, setPageSize] = useState<number>(10);
  const [page, setPage] = useState<number>(1);
  const [stage, setStage] = useState<string>('');

  useEffect(() => {
    async function load() {
      const params = new URLSearchParams({ page: String(page) });
      if (stage) {
        params.set('status', stage);
      }
      const res = await fetch(`/api/invoices/all?${params.toString()}`);
      if (res.ok) {
        const data: InvoicePage = await res.json();
        setInvoices(data.invoices);
        setTotal(data.total);
        setPageSize(data.pageSize);
      }
    }
    void load();
  }, [page, stage]);

  // Changing the stage filter starts again from the first page so the count and page number stay
  // consistent with what is shown.
  function changeStage(next: string): void {
    setStage(next);
    setPage(1);
  }

  const totalPages = Math.max(1, Math.ceil(total / pageSize));

  return (
    <section data-testid="all-invoices-page">
      <h1>Invoices</h1>
      <label>
        Stage
        <select
          data-testid="invoice-status-filter"
          value={stage}
          onChange={(e) => changeStage(e.target.value)}
        >
          <option value="">All stages</option>
          {STAGES.map((s) => (
            <option key={s} value={s}>
              {s}
            </option>
          ))}
        </select>
      </label>
      {total === 0 ? (
        <p data-testid="all-invoices-empty">No invoices yet.</p>
      ) : (
        <>
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
          <nav data-testid="invoice-pager">
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
              disabled={page >= totalPages}
              onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
            >
              Next
            </button>
          </nav>
        </>
      )}
    </section>
  );
}
