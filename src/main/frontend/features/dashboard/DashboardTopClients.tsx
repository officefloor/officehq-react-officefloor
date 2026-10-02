import React, { useEffect, useState } from 'react';

// The dashboard's "top clients" panel owns its own state and data loading (no global store). It lists
// the home screen's five biggest debtors, ranked by how much each still owes (largest first) — the
// money-owed figure served pre-computed and pre-ranked by /api/dashboard/top-clients.
type TopClient = {
  id: number;
  name: string;
  owed: number;
};

function formatAmount(amount: number): string {
  return `$${Number(amount).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
}

export function DashboardTopClients() {
  const [clients, setClients] = useState<TopClient[]>([]);

  useEffect(() => {
    async function load() {
      const res = await fetch('/api/dashboard/top-clients');
      setClients(await res.json());
    }
    void load();
  }, []);

  return (
    <section data-testid="dashboard-top-clients">
      <h2>Top clients</h2>
      <table>
        <thead>
          <tr>
            <th>Client</th>
            <th>Owed</th>
          </tr>
        </thead>
        <tbody>
          {clients.map((client) => (
            <tr key={client.id} data-testid={`top-client-row-${client.id}`}>
              <td data-testid="top-client-name">{client.name}</td>
              <td data-testid="top-client-amount">{formatAmount(client.owed)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
