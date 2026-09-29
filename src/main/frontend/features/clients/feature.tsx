import React, { useEffect, useState } from 'react';
import type { Feature } from '../../router/routes';

// Clients feature: add a client (name + email) and list every client. Owns its own state; talks to
// its own /api/clients endpoints. data-testid anchors follow the spec's conventions.
type Client = { id: number; name: string; email: string };

function ClientsPage() {
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

  async function submit(e: React.FormEvent) {
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
    <section data-testid="clients">
      <form data-testid="client-form" onSubmit={submit}>
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
        <button data-testid="client-form-submit" type="submit">
          Add client
        </button>
      </form>

      {clients.length === 0 ? (
        <p data-testid="clients-empty">No clients yet.</p>
      ) : (
        <table data-testid="clients-table">
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

export const feature: Feature = { id: 'clients', label: 'Clients', Page: ClientsPage };
