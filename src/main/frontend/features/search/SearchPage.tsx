import React, { useEffect, useState } from 'react';

// Global search feature: owns its own state (CLAUDE.md — features own their state, no global store).
// One box looks across BOTH clients and projects; the server (GET /api/search) matches each by name
// and returns them grouped by kind, which we render as a "Clients" group and a "Projects" group.
type Client = { id: number; name: string; email: string };
type Project = { id: number; name: string; clientId: number; clientName: string };
type SearchResults = { clients: Client[]; projects: Project[] };

const EMPTY: SearchResults = { clients: [], projects: [] };

export function SearchPage() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<SearchResults>(EMPTY);

  useEffect(() => {
    const term = query.trim();
    if (!term) {
      setResults(EMPTY);
      return;
    }
    let cancelled = false;
    void (async () => {
      const res = await fetch(`/api/search?q=${encodeURIComponent(term)}`);
      if (res.ok && !cancelled) {
        setResults(await res.json());
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [query]);

  return (
    <section data-testid="search-page">
      <h1>Search</h1>
      <input
        data-testid="global-search"
        placeholder="Search clients and jobs"
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
        <h2>Jobs</h2>
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
