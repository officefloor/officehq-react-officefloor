import React, { useState } from 'react';
import { Client, createClient, validateEmail, DUPLICATE_EMAIL, INVALID_EMAIL } from './clientsApi';

// The "add a client" form. Owns its own draft (name/email/error) so the page doesn't have to, and
// reports a successful creation back through onCreated.
export function ClientForm({
  takenEmails,
  onCreated,
}: {
  takenEmails: string[];
  onCreated: (client: Client) => void;
}) {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [emailError, setEmailError] = useState('');

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    const localError = validateEmail(email, takenEmails);
    if (localError) {
      setEmailError(localError);
      return;
    }
    setEmailError('');
    const res = await createClient({ name, email });
    if (res.ok) {
      onCreated(await res.json());
      setName('');
      setEmail('');
    } else {
      setEmailError(res.status === 409 ? DUPLICATE_EMAIL : INVALID_EMAIL);
    }
  }

  return (
    <form data-testid="client-form" onSubmit={onSubmit}>
      <input
        data-testid="client-form-name"
        placeholder="Name"
        value={name}
        onChange={(e) => setName(e.target.value)}
      />
      <input
        data-testid="client-form-email"
        placeholder="Email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
      />
      {emailError && (
        <span data-testid="client-form-email-error" role="alert">
          {emailError}
        </span>
      )}
      <button type="submit" data-testid="client-form-submit">
        Add client
      </button>
    </form>
  );
}
