import React, { useState } from 'react';
import { Client, updateClient, validateEmail, DUPLICATE_EMAIL, INVALID_EMAIL } from './clientsApi';

// The "correct a client" form. Pre-filled from the client being edited so a small fix (a renamed
// company, a moved mailbox) is a quick edit. Owns its own draft; reports the saved row back through
// onSaved. `takenEmails` are the OTHER clients' emails — the client keeps its own address freely.
export function ClientEditForm({
  client,
  takenEmails,
  onSaved,
  onCancel,
}: {
  client: Client;
  takenEmails: string[];
  onSaved: (client: Client) => void;
  onCancel: () => void;
}) {
  const [name, setName] = useState(client.name);
  const [email, setEmail] = useState(client.email);
  const [emailError, setEmailError] = useState('');

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    const localError = validateEmail(email, takenEmails);
    if (localError) {
      setEmailError(localError);
      return;
    }
    setEmailError('');
    const res = await updateClient({ id: client.id, name, email });
    if (res.ok) {
      onSaved(await res.json());
    } else {
      setEmailError(res.status === 409 ? DUPLICATE_EMAIL : INVALID_EMAIL);
    }
  }

  return (
    <form data-testid="client-edit-form" onSubmit={onSubmit}>
      <input
        data-testid="client-edit-form-name"
        placeholder="Name"
        value={name}
        onChange={(e) => setName(e.target.value)}
      />
      <input
        data-testid="client-edit-form-email"
        placeholder="Email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
      />
      {emailError && (
        <span data-testid="client-edit-form-email-error" role="alert">
          {emailError}
        </span>
      )}
      <button type="submit" data-testid="client-edit-form-submit">
        Save changes
      </button>
      <button type="button" data-testid="client-edit-form-cancel" onClick={onCancel}>
        Cancel
      </button>
    </form>
  );
}
