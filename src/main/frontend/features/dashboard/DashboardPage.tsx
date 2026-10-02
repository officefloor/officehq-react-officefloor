import { useEffect, useState } from 'react';

// Home screen: a cross-feature summary. It shows how many clients and projects the user has, and
// how much money is still owed (the sum of UNPAID invoice amounts across all projects). The page
// owns its own state and reads the single /api/dashboard aggregate; it does not import other
// features.
type Summary = { clients: number; projects: number; outstanding: number };

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

  return (
    <section data-testid="dashboard-page">
      <h1>Dashboard</h1>

      <div data-testid="dashboard-clients">
        <span>Clients</span>
        <strong data-testid="dashboard-clients-count">{clients}</strong>
      </div>

      <div data-testid="dashboard-projects">
        <span>Projects</span>
        <strong data-testid="dashboard-projects-count">{projects}</strong>
      </div>

      <div data-testid="dashboard-outstanding">
        <span>Outstanding</span>
        <strong data-testid="dashboard-outstanding-total">{outstanding.toFixed(2)}</strong>
      </div>
    </section>
  );
}
