import React, { useEffect, useState } from 'react';
import { ClientSummary } from './ClientSummary';
import { ClientCurrency } from './ClientCurrency';
import { ClientStatement } from './ClientStatement';
import { ClientPayment } from './ClientPayment';
import { ClientProjects } from './ClientProjects';
import { ClientContacts } from './ClientContacts';

// The clients feature owns its own state, data loading and layout (no global store). It lists every
// client and adds a new one by name + email.
type Client = { id: number; name: string; email: string };

// A proper email: something, '@', something, '.', something — no whitespace. Matches the server
// (CreateClientLogic) and schema (V2) checks so the three layers agree on what is acceptable.
const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function ClientsPage() {
  const [clients, setClients] = useState<Client[]>([]);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [emailError, setEmailError] = useState('');
  const [search, setSearch] = useState('');
  // How the list is ordered: by name (A–Z) or by how much each client owes (most first).
  const [sort, setSort] = useState<'name' | 'outstanding'>('name');
  // Per-client outstanding totals, keyed by client id, for the "owes" ordering.
  const [outstanding, setOutstanding] = useState<Record<number, number>>({});
  const [openClientId, setOpenClientId] = useState<number | null>(null);
  // Whether the archived (tucked-away) clients are being shown instead of the active list.
  const [showArchived, setShowArchived] = useState(false);
  // The archived clients, for the "show archived" view where they can be brought back.
  const [archivedClients, setArchivedClients] = useState<Client[]>([]);

  // The client currently being corrected from the list, plus the edited field values.
  const [editingClientId, setEditingClientId] = useState<number | null>(null);
  const [editName, setEditName] = useState('');
  const [editEmail, setEditEmail] = useState('');
  const [editError, setEditError] = useState('');

  async function load() {
    const res = await fetch('/api/clients');
    setClients(await res.json());
    const arch = await fetch('/api/clients/archived');
    setArchivedClients(await arch.json());
    const owed = await fetch('/api/clients/outstanding');
    const rows: { id: number; outstanding: number }[] = await owed.json();
    setOutstanding(Object.fromEntries(rows.map((r) => [r.id, Number(r.outstanding)])));
  }

  useEffect(() => {
    void load();
  }, []);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();

    // Every client needs a proper email — block the save and surface the error if it is missing or
    // malformed, so no row is created.
    if (!EMAIL.test(email.trim())) {
      setEmailError('A proper email address is required');
      return;
    }
    setEmailError('');

    const res = await fetch('/api/clients', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email: email.trim() }),
    });
    if (!res.ok) {
      // A 409 means the email is already taken by another client; anything else is a bad address.
      setEmailError(
        res.status === 409
          ? 'A client with this email already exists'
          : 'A proper email address is required',
      );
      return;
    }
    setName('');
    setEmail('');
    await load();
  }

  // Tuck a client away: archive it so it drops off this list and the search while being retained.
  async function onArchive(id: number) {
    await fetch(`/api/clients/${id}/archive`, { method: 'POST' });
    await load();
  }

  // Bring a tucked-away client back: clear its archived flag so it returns to the main list.
  async function onRestore(id: number) {
    await fetch(`/api/clients/${id}/restore`, { method: 'POST' });
    await load();
  }

  // Open the inline edit form for a client, pre-filled with its current name and email.
  function onEdit(c: Client) {
    setEditingClientId(c.id);
    setEditName(c.name);
    setEditEmail(c.email);
    setEditError('');
  }

  // Save the corrected name and email for the client being edited.
  async function onEditSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (editingClientId === null) return;

    if (!EMAIL.test(editEmail.trim())) {
      setEditError('A proper email address is required');
      return;
    }
    setEditError('');

    const res = await fetch(`/api/clients/${editingClientId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: editName, email: editEmail.trim() }),
    });
    if (!res.ok) {
      setEditError(
        res.status === 409
          ? 'A client with this email already exists'
          : 'A proper email address is required',
      );
      return;
    }
    setEditingClientId(null);
    await load();
  }

  // Case-insensitive filter on the client name; an empty box shows every client.
  const q = search.trim().toLowerCase();
  const filtered = q ? clients.filter((c) => c.name.toLowerCase().includes(q)) : clients;
  // Order the filtered rows: alphabetically by name, or by how much each client owes (most first).
  const shown = [...filtered].sort((a, b) =>
    sort === 'outstanding'
      ? (outstanding[b.id] ?? 0) - (outstanding[a.id] ?? 0)
      : a.name.localeCompare(b.name),
  );

  return (
    <section data-testid="clients-section">
      <h1>Clients</h1>

      <form data-testid="client-form" onSubmit={onSubmit}>
        <input
          data-testid="client-form-name"
          placeholder="Name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <input
          data-testid="client-form-email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        {emailError && (
          <p data-testid="client-form-email-error" role="alert">
            {emailError}
          </p>
        )}
        <button type="submit" data-testid="client-form-submit">
          Add client
        </button>
      </form>

      <input
        data-testid="client-search"
        placeholder="Search clients by name"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
      />

      <select
        data-testid="client-sort"
        value={sort}
        onChange={(e) => setSort(e.target.value as 'name' | 'outstanding')}
      >
        <option value="name">Name</option>
        <option value="outstanding">Amount owed</option>
      </select>

      <button
        type="button"
        data-testid="clients-show-archived"
        onClick={() => setShowArchived((v) => !v)}
      >
        {showArchived ? 'Show active' : 'Show archived'}
      </button>

      {showArchived ? (
        archivedClients.length === 0 ? (
          <p data-testid="clients-archived-empty">No archived clients.</p>
        ) : (
          <table data-testid="clients-archived-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {archivedClients.map((c) => (
                <tr key={c.id} data-testid={`client-row-${c.id}`}>
                  <td data-testid="client-name">{c.name}</td>
                  <td data-testid="client-email">{c.email}</td>
                  <td>
                    <button
                      type="button"
                      data-testid={`client-restore-${c.id}`}
                      onClick={() => void onRestore(c.id)}
                    >
                      Restore
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )
      ) : shown.length === 0 ? (
        <p data-testid="clients-empty">No clients yet.</p>
      ) : (
        <table data-testid="clients-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {shown.map((c) => (
              <tr key={c.id} data-testid={`client-row-${c.id}`}>
                <td data-testid="client-name">{c.name}</td>
                <td data-testid="client-email">{c.email}</td>
                <td>
                  <button
                    type="button"
                    data-testid={`client-open-${c.id}`}
                    onClick={() => setOpenClientId(c.id)}
                  >
                    Open
                  </button>
                  <button
                    type="button"
                    data-testid={`client-edit-${c.id}`}
                    onClick={() => onEdit(c)}
                  >
                    Edit
                  </button>
                  <button
                    type="button"
                    data-testid={`client-archive-${c.id}`}
                    onClick={() => void onArchive(c.id)}
                  >
                    Archive
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {editingClientId !== null && (
        <form data-testid="client-edit-form" onSubmit={onEditSubmit}>
          <input
            data-testid="client-edit-form-name"
            placeholder="Name"
            value={editName}
            onChange={(e) => setEditName(e.target.value)}
          />
          <input
            data-testid="client-edit-form-email"
            placeholder="Email"
            value={editEmail}
            onChange={(e) => setEditEmail(e.target.value)}
          />
          {editError && (
            <p data-testid="client-edit-form-error" role="alert">
              {editError}
            </p>
          )}
          <button type="submit" data-testid="client-edit-form-submit">
            Save client
          </button>
        </form>
      )}

      {openClientId !== null ? <ClientSummary clientId={openClientId} /> : null}
      {openClientId !== null ? <ClientCurrency clientId={openClientId} /> : null}
      {openClientId !== null ? <ClientStatement clientId={openClientId} /> : null}
      {openClientId !== null ? <ClientPayment clientId={openClientId} /> : null}
      {openClientId !== null ? <ClientProjects clientId={openClientId} /> : null}
      {openClientId !== null ? <ClientContacts clientId={openClientId} /> : null}
    </section>
  );
}
