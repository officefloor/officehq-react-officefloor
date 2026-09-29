import React, { useEffect, useState } from 'react';

// Dashboard feature: a read-only home screen summarising clients, projects and money still owed.
// Owns its own state (CLAUDE.md — features own their state, no global store).
// Clients are paid in different currencies, which are never added together: the outstanding total
// is kept separate, one figure per currency.
type OutstandingByCurrency = {
  currency: string;
  amount: number;
};

type Summary = {
  clientsCount: number;
  projectsCount: number;
  outstanding: OutstandingByCurrency[];
  overdueCount: number;
};

// One "top client" row: a client ranked by how much they still owe, shown in their own currency.
type TopClient = {
  clientId: number;
  name: string;
  outstanding: number;
  currency: string;
};

// Money owed is shown with the currency's symbol and 2 decimals (e.g. 300 USD -> "$300.00",
// 200 EUR -> "€200.00").
function money(amount: number, currency = 'USD'): string {
  return Number(amount).toLocaleString('en-US', { style: 'currency', currency });
}

export function DashboardPage() {
  const [summary, setSummary] = useState<Summary | null>(null);
  const [topClients, setTopClients] = useState<TopClient[]>([]);

  useEffect(() => {
    async function load() {
      const res = await fetch('/api/dashboard');
      if (res.ok) {
        setSummary(await res.json());
      }
      const top = await fetch('/api/dashboard/top-clients');
      if (top.ok) {
        setTopClients(await top.json());
      }
    }
    void load();
  }, []);

  const outstanding = summary?.outstanding ?? [];

  return (
    <section data-testid="dashboard-page">
      <h1>Dashboard</h1>
      <dl>
        <dt>Clients</dt>
        <dd data-testid="dashboard-clients-count">{summary?.clientsCount ?? 0}</dd>
        <dt>Jobs</dt>
        <dd data-testid="dashboard-projects-count">{summary?.projectsCount ?? 0}</dd>
        <dt>Outstanding</dt>
        {outstanding.map((o) => (
          <dd key={o.currency} data-testid={`dashboard-outstanding-${o.currency}`}>
            {money(o.amount, o.currency)}
          </dd>
        ))}
        <dt>Overdue invoices</dt>
        <dd data-testid="dashboard-overdue-count">{summary?.overdueCount ?? 0}</dd>
      </dl>

      <h2>Top clients</h2>
      <ol data-testid="dashboard-top-clients">
        {topClients.map((client) => (
          <li key={client.clientId} data-testid={`top-client-row-${client.clientId}`}>
            <span data-testid="top-client-name">{client.name}</span>
            <span data-testid="top-client-amount">{money(client.outstanding, client.currency)}</span>
          </li>
        ))}
      </ol>
    </section>
  );
}
