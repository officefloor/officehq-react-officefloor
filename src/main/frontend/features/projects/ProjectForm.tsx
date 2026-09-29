import React, { useEffect, useState } from 'react';
import { Client, Project, ProjectStatus, STATUSES } from './projectModel';

// The "add a job" form. Owns its own draft state (CLAUDE.md — features own their state) and does
// the create itself; on success it hands the new project back to the list through onCreated.
export function ProjectForm({
  clients,
  onCreated,
}: {
  clients: Client[];
  onCreated: (project: Project) => void;
}) {
  const [name, setName] = useState('');
  const [clientId, setClientId] = useState('');
  const [status, setStatus] = useState<ProjectStatus>('ACTIVE');
  const [code, setCode] = useState('');
  const [codeError, setCodeError] = useState('');

  // Default to the first client once the list arrives, without clobbering a chosen value.
  useEffect(() => {
    setClientId((prev) => prev || (clients[0] ? String(clients[0].id) : ''));
  }, [clients]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setCodeError('');
    const res = await fetch('/api/projects', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, clientId: Number(clientId), status, code }),
    });
    if (res.ok) {
      onCreated(await res.json());
      setName('');
      setCode('');
    } else {
      // A duplicate code (409) is rejected server-side; flag the code field so the owner can fix it.
      setCodeError(
        res.status === 409 ? 'That code is already in use.' : 'A job code is required.',
      );
    }
  }

  return (
    <form data-testid="project-form" onSubmit={onSubmit}>
      <input
        data-testid="project-form-name"
        placeholder="Name"
        value={name}
        onChange={(e) => setName(e.target.value)}
      />
      <select
        data-testid="project-form-client"
        value={clientId}
        onChange={(e) => setClientId(e.target.value)}
      >
        {clients.map((c) => (
          <option key={c.id} value={c.id}>
            {c.name}
          </option>
        ))}
      </select>
      <select
        data-testid="project-form-status"
        value={status}
        onChange={(e) => setStatus(e.target.value as ProjectStatus)}
      >
        {STATUSES.map((s) => (
          <option key={s} value={s}>
            {s}
          </option>
        ))}
      </select>
      <input
        data-testid="project-form-code"
        placeholder="Code"
        value={code}
        onChange={(e) => setCode(e.target.value)}
      />
      {codeError && (
        <span data-testid="project-form-code-error" role="alert">
          {codeError}
        </span>
      )}
      <button type="submit" data-testid="project-form-submit">
        Add job
      </button>
    </form>
  );
}
