import { useEffect, useState } from 'react';

// Opened from the clients list: a client's detail view lists the projects done for them, scoped to
// the client via /api/clients/<id>/projects. Reuses the project-row-<id>/project-name anchors so the
// listing reads the same way wherever projects appear.
type Project = { id: number; name: string };

export function ClientDetail({ clientId }: { clientId: number }) {
  const [projects, setProjects] = useState<Project[]>([]);

  useEffect(() => {
    async function load() {
      const res = await fetch(`/api/clients/${clientId}/projects`);
      if (res.ok) {
        setProjects(await res.json());
      }
    }
    void load();
  }, [clientId]);

  return (
    <section data-testid="client-detail">
      <h2>Projects</h2>
      <table data-testid="client-projects-table">
        <thead>
          <tr>
            <th>Name</th>
          </tr>
        </thead>
        <tbody>
          {projects.map((project) => (
            <tr key={project.id} data-testid={`project-row-${project.id}`}>
              <td data-testid="project-name">{project.name}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
