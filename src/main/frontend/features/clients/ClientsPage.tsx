import React, { useEffect, useState } from 'react';

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

  async function load() {
    const res = await fetch('/api/clients');
    setClients(await res.json());
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
      setEmailError('A proper email address is required');
      return;
    }
    setName('');
    setEmail('');
    await load();
  }

  // Case-insensitive filter on the client name; an empty box shows every client.
  const q = search.trim().toLowerCase();
  const shown = q ? clients.filter((c) => c.name.toLowerCase().includes(q)) : clients;

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

      {shown.length === 0 ? (
        <p data-testid="clients-empty">No clients yet.</p>
      ) : (
        <table data-testid="clients-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
            </tr>
          </thead>
          <tbody>
            {shown.map((c) => (
              <tr key={c.id} data-testid={`client-row-${c.id}`}>
                <td data-testid="client-name">{c.name}</td>
                <td data-testid="client-email">{c.email}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
