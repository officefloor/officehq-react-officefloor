import React, { useEffect, useState } from 'react';

// Projects feature: owns its own state (CLAUDE.md — features own their state, no global store).
// A project belongs to a client; the list shows the client's NAME (joined server-side).
type Project = { id: number; name: string; clientId: number; clientName: string };
type Client = { id: number; name: string };

export function ProjectsPage() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [clients, setClients] = useState<Client[]>([]);
  const [name, setName] = useState('');
  const [clientId, setClientId] = useState('');

  async function loadProjects() {
    const res = await fetch('/api/projects');
    if (res.ok) {
      setProjects(await res.json());
    }
  }

  async function loadClients() {
    const res = await fetch('/api/clients');
    if (res.ok) {
      const list: Client[] = await res.json();
      setClients(list);
      setClientId((prev) => prev || (list[0] ? String(list[0].id) : ''));
    }
  }

  useEffect(() => {
    void loadProjects();
    void loadClients();
  }, []);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    const res = await fetch('/api/projects', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, clientId: Number(clientId) }),
    });
    if (res.ok) {
      const created: Project = await res.json();
      setProjects((prev) => [...prev, created]);
      setName('');
    }
  }

  return (
    <section data-testid="projects-page">
      <h1>Projects</h1>
      <form data-testid="project-form" onSubmit={onSubmit}>
        <input
          data-testid="project-form-name"
          placeholder="Name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <select
          data-testid="project-form-client"
          value={clientId}
          onChange={(e) => setClientId(e.target.value)}
        >
          {clients.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
        <button type="submit" data-testid="project-form-submit">
          Add project
        </button>
      </form>

      {projects.length === 0 ? (
        <p data-testid="projects-empty">No projects yet.</p>
      ) : (
        <table data-testid="projects-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Client</th>
            </tr>
          </thead>
          <tbody>
            {projects.map((p) => (
              <tr key={p.id} data-testid={`project-row-${p.id}`}>
                <td data-testid="project-name">{p.name}</td>
                <td data-testid="project-client">{p.clientName}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
