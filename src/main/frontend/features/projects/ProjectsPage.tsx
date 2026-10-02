import React, { useEffect, useState } from 'react';

// The projects feature owns its own state, data loading and layout (no global store). It lists every
// project with its client's NAME (a cross-entity join surfaced in the UI) and adds a new project by
// name + chosen client. The client select's option values are client ids.
type Project = { id: number; name: string; clientId: number; clientName: string };
type Client = { id: number; name: string; email: string };

export function ProjectsPage() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [clients, setClients] = useState<Client[]>([]);
  const [name, setName] = useState('');
  const [clientId, setClientId] = useState('');

  async function load() {
    const [pRes, cRes] = await Promise.all([fetch('/api/projects'), fetch('/api/clients')]);
    setProjects(await pRes.json());
    setClients(await cRes.json());
  }

  useEffect(() => {
    void load();
  }, []);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    const res = await fetch('/api/projects', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, clientId: Number(clientId) }),
    });
    if (!res.ok) {
      return;
    }
    setName('');
    setClientId('');
    await load();
  }

  return (
    <section data-testid="projects-section">
      <h1>Projects</h1>

      <form data-testid="project-form" onSubmit={onSubmit}>
        <input
          data-testid="project-form-name"
          placeholder="Project name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <select
          data-testid="project-form-client"
          value={clientId}
          onChange={(e) => setClientId(e.target.value)}
        >
          <option value="">Select a client</option>
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
