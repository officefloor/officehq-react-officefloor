import React, { useEffect, useState } from 'react';

// A client's contacts (name, email, role), shown when a client is opened from the clients list.
// Owns its own state (CLAUDE.md — features own their state; no global store) and a form to add one.
type Contact = { id: number; clientId: number; name: string; email: string; role: string };

// A contact needs a proper email too. Kept in sync with the server-side check in ContactsPostLogic
// and the DB CHECK constraint (V10__contacts_email_check.sql).
const EMAIL_PATTERN = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

export function ClientContacts({ clientId }: { clientId: number }) {
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [role, setRole] = useState('');
  const [emailError, setEmailError] = useState('');

  async function load() {
    const res = await fetch(`/api/contacts?clientId=${clientId}`);
    if (res.ok) {
      setContacts(await res.json());
    }
  }

  useEffect(() => {
    void load();
  }, [clientId]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!EMAIL_PATTERN.test(email.trim())) {
      setEmailError('Enter a valid email address.');
      return;
    }
    setEmailError('');
    const res = await fetch('/api/contacts', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ clientId, name, email, role }),
    });
    if (res.ok) {
      const created: Contact = await res.json();
      setContacts((prev) => [...prev, created]);
      setName('');
      setEmail('');
      setRole('');
    } else {
      setEmailError('Enter a valid email address.');
    }
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
          <span data-testid="contact-form-email-error" role="alert">
            {emailError}
          </span>
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
