import React from 'react';
import { Client } from './clientsApi';

// The tucked-away clients, shown when "Show archived" is on. Each row offers to bring the client
// back so they return to the main list. Kept separate from the main table so neither branches on
// the other's concerns (CLAUDE.md — compose, don't add per-feature ifs).
export function ArchivedClients({
  clients,
  onRestore,
}: {
  clients: Client[];
  onRestore: (id: number) => void;
}) {
  if (clients.length === 0) {
    return <p data-testid="clients-archived-empty">No archived clients.</p>;
  }
  return (
    <table data-testid="clients-archived-table">
      <thead>
        <tr>
          <th>Name</th>
          <th>Email</th>
          <th />
        </tr>
      </thead>
      <tbody>
        {clients.map((c) => (
          <tr key={c.id} data-testid={`client-archived-row-${c.id}`}>
            <td data-testid="client-name">{c.name}</td>
            <td data-testid="client-email">{c.email}</td>
            <td>
              <button
                type="button"
                data-testid={`client-restore-${c.id}`}
                onClick={() => onRestore(c.id)}
              >
                Restore
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
