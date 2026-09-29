import { useEffect, useState } from 'react';
import { ProjectBudget } from './ProjectBudget';
import { ProjectForm } from './ProjectForm';
import { ProjectInvoices } from './ProjectInvoices';
import { ProjectNotes } from './ProjectNotes';
import { ProjectTags } from './ProjectTags';
import { ProjectTasks } from './ProjectTasks';
import { Client, Project, STATUSES } from './projectModel';

// Projects feature: owns its own state (CLAUDE.md — features own their state, no global store).
// A project belongs to a client; the list shows the client's NAME (joined server-side).
type Tag = { id: number; name: string };

// The list is filtered server-side. These three controls are the whole filter state; keeping them
// in one object means changing any one control re-queries with the others left as they are.
type Filters = { includeArchived: boolean; tagId: string; status: string };
const NO_FILTERS: Filters = { includeArchived: false, tagId: '', status: '' };

export function ProjectsPage() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [clients, setClients] = useState<Client[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [filters, setFilters] = useState<Filters>(NO_FILTERS);
  const [openProjectId, setOpenProjectId] = useState<number | null>(null);

  async function loadProjects(f: Filters) {
    const params = new URLSearchParams();
    if (f.includeArchived) {
      params.set('includeArchived', 'true');
    }
    if (f.tagId) {
      params.set('tagId', f.tagId);
    }
    if (f.status) {
      params.set('status', f.status);
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
      setClients(await res.json());
    }
  }

  async function loadTags() {
    const res = await fetch('/api/tags');
    if (res.ok) {
      setTags(await res.json());
    }
  }

  useEffect(() => {
    void loadProjects(filters);
    void loadClients();
    void loadTags();
  }, []);

  // One entry point for every filter control: patch the filters, then re-query with the result.
  function applyFilters(patch: Partial<Filters>) {
    const next = { ...filters, ...patch };
    setFilters(next);
    void loadProjects(next);
  }

  function closeIfOpen(id: number) {
    setOpenProjectId((prev) => (prev === id ? null : prev));
  }

  async function onDelete(id: number) {
    const res = await fetch('/api/projects/delete', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id }),
    });
    if (res.ok) {
      setProjects((prev) => prev.filter((p) => p.id !== id));
      closeIfOpen(id);
    }
  }

  async function onArchive(id: number) {
    const res = await fetch('/api/projects/archive', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id }),
    });
    if (res.ok) {
      closeIfOpen(id);
      await loadProjects(filters);
    }
  }

  return (
    <section data-testid="projects-page">
      <h1>Jobs</h1>
      <ProjectForm
        clients={clients}
        onCreated={(created) => setProjects((prev) => [...prev, created])}
      />

      <button
        type="button"
        data-testid="projects-show-archived"
        onClick={() => applyFilters({ includeArchived: !filters.includeArchived })}
      >
        {filters.includeArchived ? 'Hide archived' : 'Show archived'}
      </button>

      <select
        data-testid="project-tag-filter"
        value={filters.tagId}
        onChange={(e) => applyFilters({ tagId: e.target.value })}
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
        value={filters.status}
        onChange={(e) => applyFilters({ status: e.target.value })}
      >
        <option value="">All statuses</option>
        {STATUSES.map((s) => (
          <option key={s} value={s}>
            {s}
          </option>
        ))}
      </select>

      {projects.length === 0 ? (
        <p data-testid="projects-empty">No jobs yet.</p>
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
          <ProjectBudget projectId={openProjectId} />
          <ProjectNotes projectId={openProjectId} />
          <ProjectTags projectId={openProjectId} />
          <ProjectTasks projectId={openProjectId} />
          <ProjectInvoices projectId={openProjectId} />
        </>
      ) : null}
    </section>
  );
}
