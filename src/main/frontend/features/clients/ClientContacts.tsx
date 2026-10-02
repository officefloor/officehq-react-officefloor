import React, { useEffect, useState } from 'react';

// A client's contacts, rendered inside the clients feature when a client is opened. Lists the
// client's contacts (name, email, role) and adds a new one. Owns its own state and data loading
// (no global store); composed, not branched.
type Contact = { id: number; clientId: number; name: string; email: string; role: string };

// A proper email: something, '@', something, '.', something — no whitespace. Matches the server
// (CreateContactLogic) and schema (V12) checks so the three layers agree on what is acceptable.
const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function ClientContacts({ clientId }: { clientId: number }) {
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [role, setRole] = useState('');
  const [emailError, setEmailError] = useState('');

  async function load() {
    const res = await fetch(`/api/clients/${clientId}/contacts`);
    setContacts(await res.json());
  }

  useEffect(() => {
    void load();
  }, [clientId]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();

    // Every contact needs a proper email — block the save and surface the error if it is missing or
    // malformed, so no row is created.
    if (!EMAIL.test(email.trim())) {
      setEmailError('A proper email address is required');
      return;
    }
    setEmailError('');

    const res = await fetch(`/api/clients/${clientId}/contacts`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email: email.trim(), role }),
    });
    if (!res.ok) {
      setEmailError('A proper email address is required');
      return;
    }
    setName('');
    setEmail('');
    setRole('');
    await load();
  }

  return (
    <section data-testid="client-contacts">
      <h2>Contacts</h2>

      <form data-testid="contact-form" onSubmit={onSubmit}>
        <input
          data-testid="contact-form-name"
          placeholder="Name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <input
          data-testid="contact-form-email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        {emailError && (
          <p data-testid="contact-form-email-error" role="alert">
            {emailError}
          </p>
        )}
        <input
          data-testid="contact-form-role"
          placeholder="Role"
          value={role}
          onChange={(e) => setRole(e.target.value)}
        />
        <button type="submit" data-testid="contact-form-submit">
          Add contact
        </button>
      </form>

      <table data-testid="client-contacts-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Role</th>
          </tr>
        </thead>
        <tbody>
          {contacts.map((c) => (
            <tr key={c.id} data-testid={`contact-row-${c.id}`}>
              <td data-testid="contact-name">{c.name}</td>
              <td data-testid="contact-email">{c.email}</td>
              <td data-testid="contact-role">{c.role}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
