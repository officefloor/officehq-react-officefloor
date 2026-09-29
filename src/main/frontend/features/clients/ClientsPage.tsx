import React, { useEffect, useState } from 'react';
import { Client, archiveClient, fetchClients } from './clientsApi';
import { ClientForm } from './ClientForm';
import { ClientsTable } from './ClientsTable';
import { ClientDetails } from './ClientDetails';

// Clients feature: owns its own state (CLAUDE.md — features own their state, no global store).
// The page orchestrates; the form, table and detail panels each keep their own concerns.
export function ClientsPage() {
  const [clients, setClients] = useState<Client[]>([]);
  const [search, setSearch] = useState('');
  const [openClientId, setOpenClientId] = useState<number | null>(null);

  async function load() {
    const loaded = await fetchClients();
    if (loaded) {
      setClients(loaded);
    }
  }

  useEffect(() => {
    void load();
  }, []);

  // The list can get long; filter by name, case-insensitively. Empty box shows every client.
  const query = search.trim().toLowerCase();
  const visibleClients = query
    ? clients.filter((c) => c.name.toLowerCase().includes(query))
    : clients;

  function onCreated(created: Client) {
    setClients((prev) => [...prev, created]);
  }

  // Tuck a client away: it drops off the list and the search, but the row is kept server-side.
  async function onArchive(id: number) {
    const res = await archiveClient(id);
    if (res.ok) {
      setClients((prev) => prev.filter((c) => c.id !== id));
      setOpenClientId((prev) => (prev === id ? null : prev));
    }
  }

  return (
    <section data-testid="clients-page">
      <h1>Clients</h1>
      <ClientForm takenEmails={clients.map((c) => c.email)} onCreated={onCreated} />

      <input
        data-testid="client-search"
        placeholder="Search by name"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
      />

      {clients.length === 0 ? (
        <p data-testid="clients-empty">No clients yet.</p>
      ) : (
        <ClientsTable clients={visibleClients} onOpen={setOpenClientId} onArchive={onArchive} />
      )}

      {openClientId !== null ? <ClientDetails clientId={openClientId} /> : null}
    </section>
  );
}
