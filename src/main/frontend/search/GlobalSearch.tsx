import React, { useEffect, useState } from 'react';

// One global search box that looks across both clients and projects at once, showing the matches
// grouped by kind. App-level chrome (mounted in the shell, always visible), so it owns its own
// search state and talks to its own /api/search endpoint. It reuses the client-row-<id>/client-name
// and project-row-<id>/project-name anchors within its own grouped result panels.
type SearchClient = { id: number; name: string; email: string };
type SearchProject = { id: number; name: string; clientId: number; clientName: string };
type Results = { clients: SearchClient[]; projects: SearchProject[] };

const EMPTY: Results = { clients: [], projects: [] };

// One matched group (clients, or projects) rendered as a table. Both groups are the same shape — a
// row per hit showing its name — so they share this one block; each passes the testids that anchor
// its own group, rows and name cell.
function ResultGroup({
  groupTestid,
  rowPrefix,
  nameTestid,
  rows,
}: {
  groupTestid: string;
  rowPrefix: string;
  nameTestid: string;
  rows: { id: number; name: string }[];
}) {
  return (
    <div data-testid={groupTestid}>
      <table>
        <tbody>
          {rows.map((row) => (
            <tr key={row.id} data-testid={`${rowPrefix}-${row.id}`}>
              <td data-testid={nameTestid}>{row.name}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

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
      <ResultGroup
        groupTestid="search-clients"
        rowPrefix="client-row"
        nameTestid="client-name"
        rows={results.clients}
      />
      <ResultGroup
        groupTestid="search-projects"
        rowPrefix="project-row"
        nameTestid="project-name"
        rows={results.projects}
      />
    </section>
  );
}
