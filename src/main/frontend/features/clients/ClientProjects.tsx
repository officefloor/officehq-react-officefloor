import React, { useEffect, useState } from 'react';

// A client's projects, rendered inside the clients feature when a client is opened. Lists the
// projects done for that client (reusing the project-row-<id>/project-name anchors). Owns its own
// state and data loading (no global store); composed, not branched.
//
// By default it shows only the client's ACTIVE projects — the live work. The finished ones (any
// non-ACTIVE status) and the hidden ones (archived) are kept off the list until the user asks to
// see everything via the show-all toggle; the server returns the full list and this filters it.
type Project = {
  id: number;
  name: string;
  clientId: number;
  clientName: string;
  archived: boolean;
  status: string;
};

export function ClientProjects({ clientId }: { clientId: number }) {
  const [projects, setProjects] = useState<Project[]>([]);
  const [showAll, setShowAll] = useState(false);

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

  const visible = showAll
    ? projects
    : projects.filter((p) => p.status === 'ACTIVE' && !p.archived);

  return (
    <section data-testid="client-detail">
      <h2>Projects</h2>

      <button
        type="button"
        data-testid="client-projects-show-all"
        aria-pressed={showAll}
        onClick={() => setShowAll((s) => !s)}
      >
        {showAll ? 'Show active only' : 'Show finished and hidden'}
      </button>

      {visible.length === 0 ? (
        <p data-testid="client-projects-empty">No projects yet.</p>
      ) : (
        <table data-testid="client-projects-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {visible.map((p) => (
              <tr key={p.id} data-testid={`project-row-${p.id}`}>
                <td data-testid="project-name">{p.name}</td>
                <td data-testid="project-status">{p.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
