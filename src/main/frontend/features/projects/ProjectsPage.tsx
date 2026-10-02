import { useEffect, useState, type FormEvent } from 'react';
import { ProjectDetail } from './ProjectDetail';

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
  const [openProjectId, setOpenProjectId] = useState<number | null>(null);
  const [showArchived, setShowArchived] = useState(false);

  async function loadProjects() {
    const res = await fetch(showArchived ? '/api/projects?includeArchived=true' : '/api/projects');
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

  // Re-fetch when the archived toggle flips so tucked-away projects appear or disappear.
  useEffect(() => {
    void loadProjects();
  }, [showArchived]);

  async function onArchive(id: number) {
    const res = await fetch(`/api/projects/${id}/archive`, { method: 'POST' });
    if (res.ok) {
      if (openProjectId === id) {
        setOpenProjectId(null);
      }
      await loadProjects();
    }
  }

  async function onDelete(id: number) {
    const res = await fetch(`/api/projects/${id}/delete`, { method: 'POST' });
    if (res.ok) {
      if (openProjectId === id) {
        setOpenProjectId(null);
      }
      await loadProjects();
    }
  }

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

      <button
        data-testid="projects-show-archived"
        type="button"
        onClick={() => setShowArchived((shown) => !shown)}
      >
        {showArchived ? 'Hide archived' : 'Show archived'}
      </button>

      {projects.length === 0 ? (
        <p data-testid="projects-empty">No projects yet.</p>
      ) : (
        <table data-testid="projects-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Client</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {projects.map((project) => (
              <tr key={project.id} data-testid={`project-row-${project.id}`}>
                <td data-testid="project-name">{project.name}</td>
                <td data-testid="project-client">{project.clientName}</td>
                <td>
                  <button
                    data-testid={`project-open-${project.id}`}
                    type="button"
                    onClick={() => setOpenProjectId(project.id)}
                  >
                    Open
                  </button>
                  <button
                    data-testid={`project-archive-${project.id}`}
                    type="button"
                    onClick={() => void onArchive(project.id)}
                  >
                    Archive
                  </button>
                  <button
                    data-testid={`project-delete-${project.id}`}
                    type="button"
                    onClick={() => void onDelete(project.id)}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {openProjectId !== null && <ProjectDetail projectId={openProjectId} />}
    </section>
  );
}
