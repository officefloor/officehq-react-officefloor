import React, { useEffect, useState } from 'react';
import type { Feature } from '../../router/routes';

// Projects feature: add a project and pick which client it is for, then list every project showing
// the client's NAME. Owns its own state; talks to its own /api/projects endpoints and reads
// /api/clients to populate the client picker. data-testid anchors follow the spec's conventions.
type Client = { id: number; name: string; email: string };
type Project = { id: number; name: string; clientId: number; clientName: string };

function ProjectsPage() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [clients, setClients] = useState<Client[]>([]);
  const [name, setName] = useState('');
  const [clientId, setClientId] = useState('');

  async function loadProjects() {
    const res = await fetch('/api/projects');
    setProjects(await res.json());
  }

  async function loadClients() {
    const res = await fetch('/api/clients');
    setClients(await res.json());
  }

  useEffect(() => {
    void loadProjects();
    void loadClients();
  }, []);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!name.trim() || !clientId) {
      return;
    }
    await fetch('/api/projects', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, clientId: Number(clientId) }),
    });
    setName('');
    setClientId('');
    await loadProjects();
  }

  return (
    <section data-testid="projects">
      <form data-testid="project-form" onSubmit={submit}>
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
            <option key={c.id} value={String(c.id)}>
              {c.name}
            </option>
          ))}
        </select>
        <button data-testid="project-form-submit" type="submit">
          Add project
        </button>
      </form>

      {projects.length === 0 ? (
        <p data-testid="projects-empty">No projects yet.</p>
      ) : (
        <table data-testid="projects-table">
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

export const feature: Feature = { id: 'projects', label: 'Projects', Page: ProjectsPage };
