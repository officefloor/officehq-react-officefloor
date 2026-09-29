import React, { useState } from 'react';
import { createRoot } from 'react-dom/client';
import { features } from './router/routes';

// Minimal base shell. Features register additively via the manifest router (router/routes.ts); the
// shell renders a nav link per feature and the active feature's page. Navigation is via nav-* clicks
// (no URL router needed for the specs). Every observable element carries a stable data-testid.
function App() {
  const [active, setActive] = useState<string | null>(null);
  const current = features.find((f) => f.id === active);
  return (
    <div data-testid="app-root">
      <nav data-testid="app-nav">
        {features.map((f) => (
          <button key={f.id} data-testid={`nav-${f.id}`} onClick={() => setActive(f.id)}>
            {f.label}
          </button>
        ))}
      </nav>
      <main data-testid="app-home">{current ? <current.Page /> : null}</main>
    </div>
  );
}

createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);
