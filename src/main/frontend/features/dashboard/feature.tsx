import React, { useEffect, useState } from 'react';
import type { Feature } from '../../router/routes';

// Dashboard feature: a home screen summarising the whole account — how many clients and projects
// there are, how much money is still owed (the total of every UNPAID invoice across all projects),
// and how many SENT invoices are overdue (past their due date). Read-only; owns its own state and
// reads its own /api/dashboard endpoint.
type Summary = {
  clientsCount: number;
  projectsCount: number;
  outstandingTotal: number;
  overdueCount: number;
};

// The home screen's top clients: the handful that owe the most, each with the amount owed to rank
// and display by. Read from its own /api/dashboard/top-clients endpoint.
type TopClient = {
  id: number;
  name: string;
  outstanding: number;
};

function money(amount: number): string {
  return `$${Number(amount).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
}

function DashboardPage() {
  const [summary, setSummary] = useState<Summary | null>(null);
  const [topClients, setTopClients] = useState<TopClient[]>([]);

  useEffect(() => {
    async function load() {
      const res = await fetch('/api/dashboard');
      setSummary(await res.json());
    }
    void load();
  }, []);

  useEffect(() => {
    async function load() {
      const res = await fetch('/api/dashboard/top-clients');
      setTopClients(await res.json());
    }
    void load();
  }, []);

  // One tile per headline figure. Listing them as data keeps the markup to a single repeated block
  // and puts every tile's label and value in one place to read (and relabel) at a glance.
  const tiles = summary
    ? [
        { testid: 'dashboard-clients', label: 'Clients', valueTestid: 'dashboard-clients-count', value: summary.clientsCount },
        { testid: 'dashboard-projects', label: 'Jobs', valueTestid: 'dashboard-projects-count', value: summary.projectsCount },
        { testid: 'dashboard-outstanding', label: 'Outstanding', valueTestid: 'dashboard-outstanding-total', value: money(summary.outstandingTotal) },
        { testid: 'dashboard-overdue', label: 'Overdue invoices', valueTestid: 'dashboard-overdue-count', value: summary.overdueCount },
      ]
    : [];

  return (
    <section data-testid="dashboard">
      {tiles.map((tile) => (
        <div key={tile.testid} data-testid={tile.testid}>
          <span>{tile.label}</span>
          <span data-testid={tile.valueTestid}>{tile.value}</span>
        </div>
      ))}
      <ol data-testid="dashboard-top-clients">
        {topClients.map((client) => (
          <li key={client.id} data-testid={`top-client-row-${client.id}`}>
            <span data-testid="top-client-name">{client.name}</span>
            <span data-testid="top-client-amount">{money(client.outstanding)}</span>
          </li>
        ))}
      </ol>
    </section>
  );
}

export const feature: Feature = { id: 'dashboard', label: 'Dashboard', Page: DashboardPage };
