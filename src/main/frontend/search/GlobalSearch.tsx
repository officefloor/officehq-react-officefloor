import React, { useEffect, useState } from 'react';

// One global search box that looks across both clients and projects at once, showing the matches
// grouped by kind. App-level chrome (mounted in the shell, always visible), so it owns its own
// search state and talks to its own /api/search endpoint. It reuses the client-row-<id>/client-name
// and project-row-<id>/project-name anchors within its own grouped result panels.
type SearchClient = { id: number; name: string; email: string };
type SearchProject = { id: number; name: string; clientId: number; clientName: string };
type Results = { clients: SearchClient[]; projects: SearchProject[] };

const EMPTY: Results = { clients: [], projects: [] };

export function GlobalSearch() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<Results>(EMPTY);

  useEffect(() => {
    const term = query.trim();
    if (!term) {
      setResults(EMPTY);
      return;
    }
    let cancelled = false;
    async function load() {
      const res = await fetch(`/api/search?q=${encodeURIComponent(term)}`);
      const data: Results = await res.json();
      if (!cancelled) {
        setResults(data);
      }
    }
    void load();
    return () => {
      cancelled = true;
    };
  }, [query]);

  return (
    <section data-testid="global-search-panel">
      <input
        data-testid="global-search"
        placeholder="Search clients and projects"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
      />
      <div data-testid="search-clients">
        <table>
          <tbody>
            {results.clients.map((c) => (
              <tr key={c.id} data-testid={`client-row-${c.id}`}>
                <td data-testid="client-name">{c.name}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div data-testid="search-projects">
        <table>
          <tbody>
            {results.projects.map((p) => (
              <tr key={p.id} data-testid={`project-row-${p.id}`}>
                <td data-testid="project-name">{p.name}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
