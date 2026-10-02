import { useEffect, useState } from 'react';
import { formatMoney } from '../../ui/money';

// Home screen: a cross-feature summary. It shows how many clients and projects the user has, and
// how much money is still owed (the sum of UNPAID invoice amounts across all projects). The page
// owns its own state and reads the single /api/dashboard aggregate; it does not import other
// features.
type TopClient = { id: number; name: string; owed: number };
type Summary = {
  clients: number;
  projects: number;
  outstanding: number;
  overdue: number;
  topClients: TopClient[];
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

  const clients = summary?.clients ?? 0;
  const projects = summary?.projects ?? 0;
  const outstanding = summary?.outstanding ?? 0;
  const overdue = summary?.overdue ?? 0;
  const topClients = summary?.topClients ?? [];

  return (
    <section data-testid="dashboard-page">
      <h1>Dashboard</h1>

      <div data-testid="dashboard-clients">
        <span>Clients</span>
        <strong data-testid="dashboard-clients-count">{clients}</strong>
      </div>

      <div data-testid="dashboard-projects">
        <span>Jobs</span>
        <strong data-testid="dashboard-projects-count">{projects}</strong>
      </div>

      <div data-testid="dashboard-outstanding">
        <span>Outstanding</span>
        <strong data-testid="dashboard-outstanding-total">{formatMoney(outstanding)}</strong>
      </div>

      <div data-testid="dashboard-overdue">
        <span>Overdue invoices</span>
        <strong data-testid="dashboard-overdue-count">{overdue}</strong>
      </div>

      <div data-testid="dashboard-top-clients">
        <h2>Top clients</h2>
        {topClients.map((client) => (
          <div key={client.id} data-testid={`top-client-row-${client.id}`}>
            <span data-testid="top-client-name">{client.name}</span>
            <span data-testid="top-client-amount">{formatMoney(client.owed)}</span>
          </div>
        ))}
      </div>
    </section>
  );
}
