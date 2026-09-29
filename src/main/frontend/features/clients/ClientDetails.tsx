import React from 'react';
import { ClientProjects } from './ClientProjects';
import { ClientContacts } from './ClientContacts';
import { ClientSummary } from './ClientSummary';
import { ClientStatement } from './ClientStatement';

// The panels shown for the currently opened client. ClientStatement is keyed by clientId so it
// remounts (rather than merely re-fetches) when the open client changes.
export function ClientDetails({ clientId }: { clientId: number }) {
  return (
    <>
      <ClientSummary clientId={clientId} />
      <ClientStatement key={clientId} clientId={clientId} />
      <ClientProjects clientId={clientId} />
      <ClientContacts clientId={clientId} />
    </>
  );
}
