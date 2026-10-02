import React, { useState } from 'react';
import { createRoot } from 'react-dom/client';
import { routes } from './router/routes';

// Minimal base shell. The opinionated conventions (file-based routing under router/, closed
// primitives under ui/, no global store, scoped styles) are how checkpoints add features additively
// (CLAUDE.md). Every observable element/value carries a stable data-testid, never renamed/removed.
// The nav is rendered from the feature-registered routes; clicking a nav link shows that page.
function App() {
  const [active, setActive] = useState(() => routes[0]?.id ?? '');
  const current = routes.find((r) => r.id === active);
  const Current = current?.component;

  return (
    <div data-testid="app-root">
      <nav data-testid="app-nav">
        {routes.map((r) => (
          <button
            key={r.id}
            type="button"
            data-testid={`nav-${r.id}`}
            onClick={() => setActive(r.id)}
          >
            {r.label}
          </button>
        ))}
      </nav>
      <main data-testid="app-home">{Current ? <Current /> : null}</main>
    </div>
  );
}

createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);
