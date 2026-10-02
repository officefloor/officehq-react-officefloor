import React, { useState } from 'react';
import { createRoot } from 'react-dom/client';
import { routes } from './router/routes';
import { GlobalSearch } from './features/search/GlobalSearch';

// Minimal base shell. Features register routes under features/<name>/route.tsx (discovered by
// router/routes.ts); the shell renders the nav and the selected page. Every observable element
// carries a stable data-testid, never renamed/removed (CLAUDE.md).
function App() {
  const [currentId, setCurrentId] = useState<string | null>(null);
  const current = routes.find((r) => r.id === currentId) ?? null;

  return (
    <div data-testid="app-root">
      <nav data-testid="app-nav">
        OfficeHQ
        {routes.map((r) => (
          <button
            key={r.id}
            data-testid={`nav-${r.id}`}
            onClick={() => setCurrentId(r.id)}
          >
            {r.label}
          </button>
        ))}
      </nav>
      <GlobalSearch />
      <main data-testid="app-home">{current ? <current.Component /> : null}</main>
    </div>
  );
}

createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);
