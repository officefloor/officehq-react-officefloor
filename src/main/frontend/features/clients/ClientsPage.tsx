import { useEffect, useState, type FormEvent } from 'react';

// This arm's convention: the page component owns the feature's state, data loading and layout.
type Client = { id: number; name: string; email: string };

export function ClientsPage() {
  const [clients, setClients] = useState<Client[]>([]);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');

  async function load() {
    const res = await fetch('/api/clients');
    if (res.ok) {
      setClients(await res.json());
    }
  }

  useEffect(() => {
    void load();
  }, []);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    const res = await fetch('/api/clients', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email }),
    });
    if (res.ok) {
      setName('');
      setEmail('');
      await load();
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
        <button data-testid="client-form-submit" type="submit">
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
            {clients.map((client) => (
              <tr key={client.id} data-testid={`client-row-${client.id}`}>
                <td data-testid="client-name">{client.name}</td>
                <td data-testid="client-email">{client.email}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
