import React, { useEffect, useState } from 'react';

// At-a-glance counts for an opened client: how many projects and contacts they have. Owns its own
// state (CLAUDE.md — features own their state; no global store) and reads them from the dedicated
// GET /api/clients/summary endpoint.
type Summary = { projectsCount: number; contactsCount: number };

export function ClientSummary({ clientId }: { clientId: number }) {
  const [summary, setSummary] = useState<Summary>({ projectsCount: 0, contactsCount: 0 });

  async function load() {
    const res = await fetch(`/api/clients/summary?clientId=${clientId}`);
    if (res.ok) {
      setSummary(await res.json());
    }
  }

  useEffect(() => {
    void load();
  }, [clientId]);

  return (
    <section data-testid="client-summary">
      <p>
        Projects: <span data-testid="client-projects-count">{summary.projectsCount}</span>
      </p>
      <p>
        Contacts: <span data-testid="client-contacts-count">{summary.contactsCount}</span>
      </p>
    </section>
  );
}
