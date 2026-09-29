import React, { useEffect, useState } from 'react';
import type { Feature } from '../../router/routes';

// Dashboard feature: a home screen summarising the whole account — how many clients and projects
// there are, and how much money is still owed (the total of every UNPAID invoice across all
// projects). Read-only; owns its own state and reads its own /api/dashboard endpoint.
type Summary = { clientsCount: number; projectsCount: number; outstandingTotal: number };

function money(amount: number): string {
  return Number(amount).toFixed(2);
}

function DashboardPage() {
  const [summary, setSummary] = useState<Summary | null>(null);

  useEffect(() => {
    async function load() {
      const res = await fetch('/api/dashboard');
      setSummary(await res.json());
    }
    void load();
  }, []);

  if (!summary) {
    return <section data-testid="dashboard" />;
  }

  return (
    <section data-testid="dashboard">
      <div data-testid="dashboard-clients">
        <span>Clients</span>
        <span data-testid="dashboard-clients-count">{summary.clientsCount}</span>
      </div>
      <div data-testid="dashboard-projects">
        <span>Projects</span>
        <span data-testid="dashboard-projects-count">{summary.projectsCount}</span>
      </div>
      <div data-testid="dashboard-outstanding">
        <span>Outstanding</span>
        <span data-testid="dashboard-outstanding-total">{money(summary.outstandingTotal)}</span>
      </div>
    </section>
  );
}

export const feature: Feature = { id: 'dashboard', label: 'Dashboard', Page: DashboardPage };
