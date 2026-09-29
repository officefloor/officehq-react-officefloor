import React, { useEffect, useState } from 'react';

// A client's projects, shown when a client is opened from the clients list. Owns its own state
// (CLAUDE.md — features own their state; clients does not import the projects feature). Reuses the
// project-row-<id>/project-name anchors so the projects listing reads the same in this context.
type Project = { id: number; name: string; clientId: number; clientName: string };

export function ClientProjects({ clientId }: { clientId: number }) {
  const [projects, setProjects] = useState<Project[]>([]);

  async function load() {
    const res = await fetch(`/api/projects?clientId=${clientId}`);
    if (res.ok) {
      setProjects(await res.json());
    }
  }

  useEffect(() => {
    void load();
  }, [clientId]);

  return (
    <section data-testid="client-projects">
      <h2>Projects</h2>
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
