import React, { useState } from 'react';
import { createRoot } from 'react-dom/client';
import { routes } from './router/routes';

// Minimal base shell. Feature pages register additively via router/routes (import.meta.glob over
// features/*/route.tsx); the shell just renders the nav + the active page. No per-feature branching
// here (CLAUDE.md). Every observable element/value carries a stable data-testid.
function App() {
  const [active, setActive] = useState<string | null>(routes[0]?.section ?? null);
  const current = routes.find((r) => r.section === active);
  const Page = current?.Page;

  return (
    <div data-testid="app-root">
      <nav data-testid="app-nav">
        {routes.map((r) => (
          <button
            key={r.section}
            data-testid={`nav-${r.section}`}
            onClick={() => setActive(r.section)}
          >
            {r.label}
          </button>
        ))}
      </nav>
      <main data-testid="app-home">{Page ? <Page /> : null}</main>
    </div>
  );
}

createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);
