import React from 'react';
import { Client } from './clientsApi';

// One row per client. Keeping a row its own component keeps the per-client actions together and
// easy to extend.
function ClientRow({
  client,
  onOpen,
  onArchive,
}: {
  client: Client;
  onOpen: (id: number) => void;
  onArchive: (id: number) => void;
}) {
  return (
    <tr data-testid={`client-row-${client.id}`}>
      <td data-testid="client-name">{client.name}</td>
      <td data-testid="client-email">{client.email}</td>
      <td>
        <button type="button" data-testid={`client-open-${client.id}`} onClick={() => onOpen(client.id)}>
          Open
        </button>
        <button
          type="button"
          data-testid={`client-archive-${client.id}`}
          onClick={() => onArchive(client.id)}
        >
          Archive
        </button>
      </td>
    </tr>
  );
}

export function ClientsTable({
  clients,
  onOpen,
  onArchive,
}: {
  clients: Client[];
  onOpen: (id: number) => void;
  onArchive: (id: number) => void;
}) {
  return (
    <table data-testid="clients-table">
      <thead>
        <tr>
          <th>Name</th>
          <th>Email</th>
          <th />
        </tr>
      </thead>
      <tbody>
        {clients.map((c) => (
          <ClientRow key={c.id} client={c} onOpen={onOpen} onArchive={onArchive} />
        ))}
      </tbody>
    </table>
  );
}
