import React, { useEffect, useState } from 'react';

// A client's at-a-glance counts, rendered inside the clients feature when a client is opened: how
// many projects and contacts that client has. Owns its own state and data loading (no global store);
// composed, not branched.
type Summary = { projectsCount: number; contactsCount: number };

export function ClientSummary({ clientId }: { clientId: number }) {
  const [summary, setSummary] = useState<Summary>({ projectsCount: 0, contactsCount: 0 });

  useEffect(() => {
    let active = true;
    async function load() {
      const res = await fetch(`/api/clients/${clientId}/summary`);
      const data = await res.json();
      if (active) {
        setSummary(data);
      }
    }
    void load();
    return () => {
      active = false;
    };
  }, [clientId]);

  return (
    <section data-testid="client-summary">
      <span data-testid="client-projects-label">Projects</span>
      <span data-testid="client-projects-count">{summary.projectsCount}</span>
      <span data-testid="client-contacts-label">Contacts</span>
      <span data-testid="client-contacts-count">{summary.contactsCount}</span>
    </section>
  );
}
