import React, { useEffect, useState } from 'react';

// The dashboard feature owns its own state and data loading (no global store). It is a read-only home
// summary: how many clients and projects there are, how much money is still owed (the sum of
// every SENT invoice across all projects — drafts and paid invoices are excluded), and how many
// SENT invoices are overdue (past their due date as of the dashboard's reference date). All served
// pre-computed by /api/dashboard.
type Summary = {
  clientsCount: number;
  projectsCount: number;
  outstandingTotal: number;
  overdueCount: number;
};

function formatAmount(amount: number): string {
  return `$${Number(amount).toLocaleString('en-US', {
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
        <dd data-testid="dashboard-outstanding-total">
          {summary ? formatAmount(summary.outstandingTotal) : ''}
        </dd>

        <dt>Overdue</dt>
        <dd data-testid="dashboard-overdue-count">{summary ? summary.overdueCount : ''}</dd>
      </dl>
    </section>
  );
}
