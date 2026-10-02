import React, { useEffect, useState } from 'react';
import { ProjectBudget } from './ProjectBudget';
import { ProjectInvoices } from './ProjectInvoices';
import { ProjectNotes } from './ProjectNotes';
import { ProjectTags } from './ProjectTags';
import { ProjectTasks } from './ProjectTasks';

// The projects feature owns its own state, data loading and layout (no global store). It lists every
// project with its client's NAME (a cross-entity join surfaced in the UI) and adds a new project by
// name + chosen client. The client select's option values are client ids.
type Project = {
  id: number;
  name: string;
  clientId: number;
  clientName: string;
  archived: boolean;
  status: string;
  tagIds: number[];
};
type Client = { id: number; name: string; email: string };
type Tag = { id: number; name: string };

// A project is marked active, on hold or finished; the owner picks when creating one.
const PROJECT_STATUSES = ['ACTIVE', 'ON_HOLD', 'FINISHED'];

export function ProjectsPage() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [clients, setClients] = useState<Client[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [name, setName] = useState('');
  const [clientId, setClientId] = useState('');
  const [status, setStatus] = useState('ACTIVE');
  const [budget, setBudget] = useState('');
  const [openProjectId, setOpenProjectId] = useState<number | null>(null);
  const [showArchived, setShowArchived] = useState(false);
  const [filterTagId, setFilterTagId] = useState('');
  const [filterStatus, setFilterStatus] = useState('');

  async function load() {
    const [pRes, cRes, tRes] = await Promise.all([
      fetch('/api/projects'),
      fetch('/api/clients'),
      fetch('/api/tags'),
    ]);
    setProjects(await pRes.json());
    setClients(await cRes.json());
    setTags(await tRes.json());
  }

  useEffect(() => {
    void load();
  }, []);

  async function remove(projectId: number) {
    const res = await fetch(`/api/projects/${projectId}`, { method: 'DELETE' });
    if (!res.ok) {
      return;
    }
    if (openProjectId === projectId) {
      setOpenProjectId(null);
    }
    await load();
  }

  // Archiving tucks a project away: it drops off the list unless archived ones are revealed, and is
  // retained (the server keeps the row, just flagged). Delete still removes a project outright.
  async function archive(projectId: number) {
    const res = await fetch(`/api/projects/${projectId}/archive`, { method: 'POST' });
    if (!res.ok) {
      return;
    }
    if (openProjectId === projectId) {
      setOpenProjectId(null);
    }
    await load();
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    const res = await fetch('/api/projects', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name,
        clientId: Number(clientId),
        status,
        budget: budget === '' ? 0 : Number(budget),
      }),
    });
    if (!res.ok) {
      return;
    }
    setName('');
    setClientId('');
    setStatus('ACTIVE');
    setBudget('');
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
        <select
          data-testid="project-form-status"
          value={status}
          onChange={(e) => setStatus(e.target.value)}
        >
          {PROJECT_STATUSES.map((s) => (
            <option key={s} value={s}>
              {s}
            </option>
          ))}
        </select>
        <input
          data-testid="project-form-budget"
          placeholder="Budget"
          value={budget}
          onChange={(e) => setBudget(e.target.value)}
        />
        <button type="submit" data-testid="project-form-submit">
          Add project
        </button>
      </form>

      <button
        type="button"
        data-testid="projects-show-archived"
        aria-pressed={showArchived}
        onClick={() => setShowArchived((s) => !s)}
      >
        {showArchived ? 'Hide archived' : 'Show archived'}
      </button>

      <select
        data-testid="project-tag-filter"
        value={filterTagId}
        onChange={(e) => setFilterTagId(e.target.value)}
      >
        <option value="">All tags</option>
        {tags.map((t) => (
          <option key={t.id} value={t.id}>
            {t.name}
          </option>
        ))}
      </select>

      <select
        data-testid="project-status-filter"
        value={filterStatus}
        onChange={(e) => setFilterStatus(e.target.value)}
      >
        <option value="">All statuses</option>
        {PROJECT_STATUSES.map((s) => (
          <option key={s} value={s}>
            {s}
          </option>
        ))}
      </select>

      {(() => {
        const byArchived = showArchived ? projects : projects.filter((p) => !p.archived);
        const byTag = filterTagId
          ? byArchived.filter((p) => (p.tagIds ?? []).includes(Number(filterTagId)))
          : byArchived;
        const visible = filterStatus
          ? byTag.filter((p) => p.status === filterStatus)
          : byTag;
        return visible.length === 0 ? (
          <p data-testid="projects-empty">No projects yet.</p>
        ) : (
        <table data-testid="projects-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Client</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {visible.map((p) => (
              <tr key={p.id} data-testid={`project-row-${p.id}`}>
                <td data-testid="project-name">{p.name}</td>
                <td data-testid="project-client">{p.clientName}</td>
                <td data-testid="project-status">{p.status}</td>
                <td>
                  <button
                    type="button"
                    data-testid={`project-open-${p.id}`}
                    onClick={() => setOpenProjectId(p.id)}
                  >
                    Open
                  </button>
                  <button
                    type="button"
                    data-testid={`project-archive-${p.id}`}
                    onClick={() => void archive(p.id)}
                  >
                    Archive
                  </button>
                  <button
                    type="button"
                    data-testid={`project-delete-${p.id}`}
                    onClick={() => void remove(p.id)}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        );
      })()}

      {openProjectId !== null ? (
        <>
          <ProjectBudget projectId={openProjectId} />
          <ProjectTags projectId={openProjectId} />
          <ProjectNotes projectId={openProjectId} />
          <ProjectTasks projectId={openProjectId} />
          <ProjectInvoices projectId={openProjectId} />
        </>
      ) : null}
    </section>
  );
}
