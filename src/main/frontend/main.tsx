import React from 'react';
import { createRoot } from 'react-dom/client';

// Minimal base shell. Every observable element and value carries a stable data-testid.
function App() {
  return (
    <div data-testid="app-root">
      <nav data-testid="app-nav">OfficeHQ</nav>
      <main data-testid="app-home" />
    </div>
  );
}

createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);
