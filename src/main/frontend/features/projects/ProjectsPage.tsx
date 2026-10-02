import { useEffect, useState, type FormEvent } from 'react';

// This arm's convention: the page component owns the feature's state, data loading and layout.
// A project belongs to a client; the list shows the client's NAME, and the form's client picker is
// a <select> whose option values are client ids.
type Client = { id: number; name: string; email: string };
type Project = { id: number; name: string; clientId: number; clientName: string };

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
      setClients(await res.json());
    }
  }

  useEffect(() => {
    void loadProjects();
    void loadClients();
  }, []);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    const res = await fetch('/api/projects', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, clientId: Number(clientId) }),
    });
    if (res.ok) {
      setName('');
      setClientId('');
      await loadProjects();
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
          <option value="">Select a client</option>
          {clients.map((client) => (
            <option key={client.id} value={client.id}>
              {client.name}
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
          <thead>
            <tr>
              <th>Name</th>
              <th>Client</th>
            </tr>
          </thead>
          <tbody>
            {projects.map((project) => (
              <tr key={project.id} data-testid={`project-row-${project.id}`}>
                <td data-testid="project-name">{project.name}</td>
                <td data-testid="project-client">{project.clientName}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
