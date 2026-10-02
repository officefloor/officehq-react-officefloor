import React, { useEffect, useState } from 'react';
import { DashboardTopClients } from './DashboardTopClients';

// The dashboard feature owns its own state and data loading (no global store). It is a read-only home
// summary: how many clients and projects there are, how much money is still owed — kept SEPARATE per
// currency, never added together (every SENT invoice across all projects, grouped by its client's
// currency; drafts and paid invoices are excluded) — and how many SENT invoices are overdue (past
// their due date as of the dashboard's reference date). All served pre-computed by /api/dashboard.
type CurrencyAmount = { currency: string; amount: number };
type Summary = {
  clientsCount: number;
  projectsCount: number;
  outstanding: CurrencyAmount[];
  overdueCount: number;
};

// Each client is paid in their own currency; show the right symbol for each currency's total.
const CURRENCY_SYMBOLS: Record<string, string> = { USD: '$', EUR: '€' };

function formatAmount(amount: number, currency = 'USD'): string {
  const symbol = CURRENCY_SYMBOLS[currency] ?? '$';
  return `${symbol}${Number(amount).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
}

export function DashboardPage() {
  const [summary, setSummary] = useState<Summary | null>(null);

  useEffect(() => {
    async function load() {
      const res = await fetch('/api/dashboard');
      setSummary(await res.json());
    }
    void load();
  }, []);

  return (
    <section data-testid="dashboard-section">
      <h1>Dashboard</h1>

      <dl>
        <dt>Clients</dt>
        <dd data-testid="dashboard-clients-count">{summary ? summary.clientsCount : ''}</dd>

        <dt>Jobs</dt>
        <dd data-testid="dashboard-projects-count">{summary ? summary.projectsCount : ''}</dd>

        <dt>Outstanding</dt>
        {summary
          ? summary.outstanding.map((entry) => (
              <dd
                key={entry.currency}
                data-testid={`dashboard-outstanding-${entry.currency}`}
              >
                {formatAmount(entry.amount, entry.currency)}
              </dd>
            ))
          : null}

        <dt>Overdue</dt>
        <dd data-testid="dashboard-overdue-count">{summary ? summary.overdueCount : ''}</dd>
      </dl>

      <DashboardTopClients />
    </section>
  );
}
