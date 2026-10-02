import { useEffect, useState } from 'react';

// One search box that looks across both clients and projects. It owns its own state, reads the
// single /api/search aggregate (the server does the cross-entity lookup), and shows the matches
// grouped by kind. Always visible in the shell, so it is the app's one global search.
type ClientMatch = { id: number; name: string; email: string };
type ProjectMatch = { id: number; name: string; clientId: number; clientName: string };
type Results = { clients: ClientMatch[]; projects: ProjectMatch[] };

export function GlobalSearch() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<Results>({ clients: [], projects: [] });

  useEffect(() => {
    let cancelled = false;
    async function load() {
      const res = await fetch(`/api/search?q=${encodeURIComponent(query)}`);
      if (res.ok && !cancelled) {
        setResults(await res.json());
      }
    }
    void load();
    return () => {
      cancelled = true;
    };
  }, [query]);

  return (
    <section data-testid="search-page">
      <input
        data-testid="global-search"
        placeholder="Search clients and jobs"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
      />

      <div data-testid="search-clients">
        <h2>Clients</h2>
        {results.clients.map((client) => (
          <div key={client.id} data-testid={`client-row-${client.id}`}>
            <span data-testid="client-name">{client.name}</span>
            <span data-testid="client-email">{client.email}</span>
          </div>
        ))}
      </div>

      <div data-testid="search-projects">
        <h2>Jobs</h2>
        {results.projects.map((project) => (
          <div key={project.id} data-testid={`project-row-${project.id}`}>
            <span data-testid="project-name">{project.name}</span>
            <span data-testid="project-client">{project.clientName}</span>
          </div>
        ))}
      </div>
    </section>
  );
}
