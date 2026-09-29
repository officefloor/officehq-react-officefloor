import React, { useEffect, useState } from 'react';
import { ProjectInvoices } from './ProjectInvoices';
import { ProjectNotes } from './ProjectNotes';
import { ProjectTags } from './ProjectTags';
import { ProjectTasks } from './ProjectTasks';

// Projects feature: owns its own state (CLAUDE.md — features own their state, no global store).
// A project belongs to a client; the list shows the client's NAME (joined server-side).
type ProjectStatus = 'ACTIVE' | 'ON_HOLD' | 'FINISHED';
type Project = {
  id: number;
  name: string;
  clientId: number;
  clientName: string;
  archived: boolean;
  status: ProjectStatus;
};
type Client = { id: number; name: string };
type Tag = { id: number; name: string };

const STATUSES: ProjectStatus[] = ['ACTIVE', 'ON_HOLD', 'FINISHED'];

export function ProjectsPage() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [clients, setClients] = useState<Client[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [tagFilter, setTagFilter] = useState('');
  const [name, setName] = useState('');
  const [clientId, setClientId] = useState('');
  const [status, setStatus] = useState<ProjectStatus>('ACTIVE');
  const [openProjectId, setOpenProjectId] = useState<number | null>(null);
  const [showArchived, setShowArchived] = useState(false);
  const [statusFilter, setStatusFilter] = useState('');

  async function loadProjects(
    includeArchived = showArchived,
    tagId = tagFilter,
    status = statusFilter,
  ) {
    const params = new URLSearchParams();
    if (includeArchived) {
      params.set('includeArchived', 'true');
    }
    if (tagId) {
      params.set('tagId', tagId);
    }
    if (status) {
      params.set('status', status);
    }
    const qs = params.toString();
    const res = await fetch(`/api/projects${qs ? `?${qs}` : ''}`);
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

  async function loadTags() {
    const res = await fetch('/api/tags');
    if (res.ok) {
      setTags(await res.json());
    }
  }

  useEffect(() => {
    void loadProjects();
    void loadClients();
    void loadTags();
  }, []);

  function onFilterByTag(tagId: string) {
    setTagFilter(tagId);
    void loadProjects(showArchived, tagId);
  }

  function onFilterByStatus(status: string) {
    setStatusFilter(status);
    void loadProjects(showArchived, tagFilter, status);
  }

  async function onDelete(id: number) {
    const res = await fetch('/api/projects/delete', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id }),
    });
    if (res.ok) {
      setProjects((prev) => prev.filter((p) => p.id !== id));
      setOpenProjectId((prev) => (prev === id ? null : prev));
    }
  }

  async function onArchive(id: number) {
    const res = await fetch('/api/projects/archive', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id }),
    });
    if (res.ok) {
      setOpenProjectId((prev) => (prev === id ? null : prev));
      await loadProjects();
    }
  }

  function onToggleArchived() {
    const next = !showArchived;
    setShowArchived(next);
    void loadProjects(next);
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    const res = await fetch('/api/projects', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, clientId: Number(clientId), status }),
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
        <select
          data-testid="project-form-status"
          value={status}
          onChange={(e) => setStatus(e.target.value as ProjectStatus)}
        >
          {STATUSES.map((s) => (
            <option key={s} value={s}>
              {s}
            </option>
          ))}
        </select>
        <button type="submit" data-testid="project-form-submit">
          Add project
        </button>
      </form>

      <button type="button" data-testid="projects-show-archived" onClick={onToggleArchived}>
        {showArchived ? 'Hide archived' : 'Show archived'}
      </button>

      <select
        data-testid="project-tag-filter"
        value={tagFilter}
        onChange={(e) => onFilterByTag(e.target.value)}
      >
        <option value="">All labels</option>
        {tags.map((t) => (
          <option key={t.id} value={t.id}>
            {t.name}
          </option>
        ))}
      </select>

      <select
        data-testid="project-status-filter"
        value={statusFilter}
        onChange={(e) => onFilterByStatus(e.target.value)}
      >
        <option value="">All statuses</option>
        {STATUSES.map((s) => (
          <option key={s} value={s}>
            {s}
          </option>
        ))}
      </select>

      {projects.length === 0 ? (
        <p data-testid="projects-empty">No projects yet.</p>
      ) : (
        <table data-testid="projects-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Client</th>
              <th>Status</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {projects.map((p) => (
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
                    onClick={() => void onArchive(p.id)}
                  >
                    Archive
                  </button>
                  <button
                    type="button"
                    data-testid={`project-delete-${p.id}`}
                    onClick={() => void onDelete(p.id)}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {openProjectId !== null ? (
        <>
          <ProjectNotes projectId={openProjectId} />
          <ProjectTags projectId={openProjectId} />
          <ProjectTasks projectId={openProjectId} />
          <ProjectInvoices projectId={openProjectId} />
        </>
      ) : null}
    </section>
  );
}
