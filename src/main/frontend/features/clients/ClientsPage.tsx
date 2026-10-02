import React, { useEffect, useState } from 'react';

// The clients feature owns its own state, data loading and layout (no global store). It lists every
// client and adds a new one by name + email.
type Client = { id: number; name: string; email: string };

export function ClientsPage() {
  const [clients, setClients] = useState<Client[]>([]);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');

  async function load() {
    const res = await fetch('/api/clients');
    setClients(await res.json());
  }

  useEffect(() => {
    void load();
  }, []);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    await fetch('/api/clients', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email }),
    });
    setName('');
    setEmail('');
    await load();
  }

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
        <button type="submit" data-testid="client-form-submit">
          Add client
        </button>
      </form>

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
            {clients.map((c) => (
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
