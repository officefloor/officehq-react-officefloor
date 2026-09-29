import React, { useEffect, useState } from 'react';

// Dashboard feature: a read-only home screen summarising clients, projects and money still owed.
// Owns its own state (CLAUDE.md — features own their state, no global store).
type Summary = {
  clientsCount: number;
  projectsCount: number;
  outstandingTotal: number;
  overdueCount: number;
};

export function DashboardPage() {
  const [summary, setSummary] = useState<Summary | null>(null);

  useEffect(() => {
    async function load() {
      const res = await fetch('/api/dashboard');
      if (res.ok) {
        setSummary(await res.json());
      }
    }
    void load();
  }, []);

  // Money owed is always shown with a currency symbol and 2 decimals (e.g. 300 -> "$300.00").
  const outstanding = Number(summary?.outstandingTotal ?? 0).toLocaleString('en-US', {
    style: 'currency',
    currency: 'USD',
  });

  return (
    <section data-testid="dashboard-page">
      <h1>Dashboard</h1>
      <dl>
        <dt>Clients</dt>
        <dd data-testid="dashboard-clients-count">{summary?.clientsCount ?? 0}</dd>
        <dt>Jobs</dt>
        <dd data-testid="dashboard-projects-count">{summary?.projectsCount ?? 0}</dd>
        <dt>Outstanding</dt>
        <dd data-testid="dashboard-outstanding-total">{outstanding}</dd>
        <dt>Overdue invoices</dt>
        <dd data-testid="dashboard-overdue-count">{summary?.overdueCount ?? 0}</dd>
      </dl>
    </section>
  );
}
