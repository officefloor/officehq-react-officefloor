import React, { useEffect, useState } from 'react';
import type { Feature } from '../../router/routes';

// Clients feature: add a client (name + email) and list every client. Opening a client
// (client-open-<id>) reveals the projects being done for them — the client's own projects listing,
// scoped via its /api/clients/<id>/projects endpoint. Owns its own state; talks to its own
// /api/clients endpoints. data-testid anchors follow the spec's conventions.
type Client = { id: number; name: string; email: string };
type Project = { id: number; name: string; clientId: number; clientName: string };

// A client's detail: the projects being done for them. Owns its own project state, scoped to the one
// client it is showing. Reuses the project-row-<id>/project-name anchors in a client-scoped table.
function ClientProjects({ client }: { client: Client }) {
  const [projects, setProjects] = useState<Project[]>([]);

  useEffect(() => {
    async function load() {
      const res = await fetch(`/api/clients/${client.id}/projects`);
      setProjects(await res.json());
    }
    void load();
  }, [client.id]);

  return (
    <table data-testid="client-projects-table">
      <tbody>
        {projects.map((p) => (
          <tr key={p.id} data-testid={`project-row-${p.id}`}>
            <td data-testid="project-name">{p.name}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

// Every client needs a proper email. Same shape the server enforces (see CreateClient) so the UI
// and the API agree on what "valid" means.
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function ClientsPage() {
  const [clients, setClients] = useState<Client[]>([]);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [emailError, setEmailError] = useState('');
  // Client-side name filter: the list can get long, so a search box narrows it. Case-insensitive
  // substring match on the client name; an empty box shows everyone.
  const [search, setSearch] = useState('');
  const [openId, setOpenId] = useState<number | null>(null);

  async function load() {
    const res = await fetch('/api/clients');
    setClients(await res.json());
  }

  useEffect(() => {
    void load();
  }, []);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!EMAIL_RE.test(email.trim())) {
      setEmailError('Enter a valid email address.');
      return;
    }
    setEmailError('');
    await fetch('/api/clients', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email }),
    });
    setName('');
    setEmail('');
    await load();
  }

  const visible = clients.filter((c) =>
    c.name.toLowerCase().includes(search.trim().toLowerCase()),
  );

  const open = clients.find((c) => c.id === openId) ?? null;

  return (
    <section data-testid="clients">
      <input
        data-testid="client-search"
        placeholder="Search clients by name"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
      />
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
        {emailError && (
          <p data-testid="client-form-email-error" role="alert">
            {emailError}
          </p>
        )}
        <button data-testid="client-form-submit" type="submit">
          Add client
        </button>
      </form>

      {clients.length === 0 ? (
        <p data-testid="clients-empty">No clients yet.</p>
      ) : (
        <table data-testid="clients-table">
          <tbody>
            {visible.map((c) => (
              <tr key={c.id} data-testid={`client-row-${c.id}`}>
                <td data-testid="client-name">{c.name}</td>
                <td data-testid="client-email">{c.email}</td>
                <td>
                  <button data-testid={`client-open-${c.id}`} onClick={() => setOpenId(c.id)}>
                    Open
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {open && <ClientProjects key={open.id} client={open} />}
    </section>
  );
}

export const feature: Feature = { id: 'clients', label: 'Clients', Page: ClientsPage };
