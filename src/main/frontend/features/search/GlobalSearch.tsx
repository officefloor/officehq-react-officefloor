import React, { useEffect, useState } from 'react';

// One search box across the whole book: it queries the server's /api/search and shows the matches
// grouped by kind — clients under one heading, projects under another. This feature owns its own
// state and data loading (no global store); it is composed into the shell so it is always on hand.
type Client = { id: number; name: string; email: string };
type Project = { id: number; name: string; clientId: number; clientName: string };
type Results = { clients: Client[]; projects: Project[] };

const EMPTY: Results = { clients: [], projects: [] };

export function GlobalSearch() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<Results>(EMPTY);

  useEffect(() => {
    const q = query.trim();
    if (!q) {
      setResults(EMPTY);
      return;
    }
    let live = true;
    void (async () => {
      const res = await fetch(`/api/search?q=${encodeURIComponent(q)}`);
      const data: Results = res.ok ? await res.json() : EMPTY;
      if (live) setResults(data);
    })();
    return () => {
      live = false;
    };
  }, [query]);

  return (
    <section data-testid="global-search-section">
      <input
        data-testid="global-search"
        placeholder="Search clients and projects"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
      />

      <div data-testid="search-clients">
        <h2>Clients</h2>
        {results.clients.map((c) => (
          <div key={c.id} data-testid={`client-row-${c.id}`}>
            <span data-testid="client-name">{c.name}</span>
            <span data-testid="client-email">{c.email}</span>
          </div>
        ))}
      </div>

      <div data-testid="search-projects">
        <h2>Projects</h2>
        {results.projects.map((p) => (
          <div key={p.id} data-testid={`project-row-${p.id}`}>
            <span data-testid="project-name">{p.name}</span>
            <span data-testid="project-client">{p.clientName}</span>
          </div>
        ))}
      </div>
    </section>
  );
}
