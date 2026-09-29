import React, { useEffect, useState } from 'react';

// A client's projects, shown when a client is opened from the clients list. Owns its own state
// (CLAUDE.md — features own their state; clients does not import the projects feature). Reuses the
// project-row-<id>/project-name anchors so the projects listing reads the same in this context.
type Project = { id: number; name: string; clientId: number; clientName: string };

export function ClientProjects({ clientId }: { clientId: number }) {
  const [projects, setProjects] = useState<Project[]>([]);
  // Show only ACTIVE (and non-archived) projects by default; the toggle reveals every status,
  // archived (hidden) ones included — that is the "also see finished and hidden" ask.
  const [showAll, setShowAll] = useState(false);

  async function load(all: boolean) {
    const query = all
      ? `clientId=${clientId}&includeArchived=true`
      : `clientId=${clientId}&status=ACTIVE`;
    const res = await fetch(`/api/projects?${query}`);
    if (res.ok) {
      setProjects(await res.json());
    }
  }

  useEffect(() => {
    void load(showAll);
  }, [clientId, showAll]);

  return (
    <section data-testid="client-projects">
      <h2>Projects</h2>
      <button
        type="button"
        data-testid="client-projects-show-all"
        aria-pressed={showAll}
        onClick={() => setShowAll((v) => !v)}
      >
        {showAll ? 'Show active only' : 'Show finished and hidden'}
      </button>
      {projects.length === 0 ? (
        <p data-testid="client-projects-empty">No projects yet.</p>
      ) : (
        <table data-testid="client-projects-table">
          <thead>
            <tr>
              <th>Name</th>
            </tr>
          </thead>
          <tbody>
            {projects.map((p) => (
              <tr key={p.id} data-testid={`project-row-${p.id}`}>
                <td data-testid="project-name">{p.name}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
