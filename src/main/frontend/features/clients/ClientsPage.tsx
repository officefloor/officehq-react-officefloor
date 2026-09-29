import React, { useEffect, useState } from 'react';

// Clients feature: owns its own state (CLAUDE.md — features own their state, no global store).
type Client = { id: number; name: string; email: string };

// Every client needs a proper email address. Kept in sync with the server-side check in
// ClientsPostLogic and the DB CHECK constraint (V2__clients_email_check.sql).
const EMAIL_PATTERN = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

export function ClientsPage() {
  const [clients, setClients] = useState<Client[]>([]);
  const [search, setSearch] = useState('');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [emailError, setEmailError] = useState('');

  // The list can get long; filter by name, case-insensitively. Empty box shows every client.
  const query = search.trim().toLowerCase();
  const visibleClients = query
    ? clients.filter((c) => c.name.toLowerCase().includes(query))
    : clients;

  async function load() {
    const res = await fetch('/api/clients');
    if (res.ok) {
      setClients(await res.json());
    }
  }

  useEffect(() => {
    void load();
  }, []);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!EMAIL_PATTERN.test(email.trim())) {
      setEmailError('Enter a valid email address.');
      return;
    }
    setEmailError('');
    const res = await fetch('/api/clients', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email }),
    });
    if (res.ok) {
      const created: Client = await res.json();
      setClients((prev) => [...prev, created]);
      setName('');
      setEmail('');
    } else {
      setEmailError('Enter a valid email address.');
    }
  }

  return (
    <section data-testid="clients-page">
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
          <span data-testid="client-form-email-error" role="alert">
            {emailError}
          </span>
        )}
        <button type="submit" data-testid="client-form-submit">
          Add client
        </button>
      </form>

      <input
        data-testid="client-search"
        placeholder="Search by name"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
      />

      {clients.length === 0 ? (
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
            {visibleClients.map((c) => (
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
