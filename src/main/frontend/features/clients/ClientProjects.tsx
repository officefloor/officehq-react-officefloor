import React, { useEffect, useState } from 'react';

// A client's projects, rendered inside the clients feature when a client is opened. Lists the
// projects done for that client (reusing the project-row-<id>/project-name anchors). Owns its own
// state and data loading (no global store); composed, not branched.
type Project = { id: number; name: string; clientId: number; clientName: string };

export function ClientProjects({ clientId }: { clientId: number }) {
  const [projects, setProjects] = useState<Project[]>([]);

  useEffect(() => {
    let active = true;
    async function load() {
      const res = await fetch(`/api/clients/${clientId}/projects`);
      const data = await res.json();
      if (active) {
        setProjects(data);
      }
    }
    void load();
    return () => {
      active = false;
    };
  }, [clientId]);

  return (
    <section data-testid="client-detail">
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
