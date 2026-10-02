import { useEffect, useState, type FormEvent } from 'react';
import { ClientStatement } from './ClientStatement';

// Opened from the clients list: a client's detail view lists the projects done for them, scoped to
// the client via /api/clients/<id>/projects. Reuses the project-row-<id>/project-name anchors so the
// listing reads the same way wherever projects appear. It also keeps the client's contacts (name,
// email, role), scoped the same way via /api/clients/<id>/contacts, with a form to add one.
type Project = { id: number; name: string };
type Contact = { id: number; name: string; email: string; role: string; primary: boolean };
type Counts = { projects: number; contacts: number };

// A contact must carry a proper email address too, same guard as clients: a single non-whitespace
// local part, an @, and a dotted domain.
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function ClientDetail({ clientId }: { clientId: number }) {
  const [projects, setProjects] = useState<Project[]>([]);
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [counts, setCounts] = useState<Counts>({ projects: 0, contacts: 0 });
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [role, setRole] = useState('');
  const [emailError, setEmailError] = useState(false);
  const [statementOpen, setStatementOpen] = useState(false);
  // By default a client's page shows only their ACTIVE projects — the work still in play. Flipping
  // this reveals the finished and hidden (archived) ones too, by asking the server for them all.
  const [showAll, setShowAll] = useState(false);

  async function loadContacts() {
    const res = await fetch(`/api/clients/${clientId}/contacts`);
    if (res.ok) {
      setContacts(await res.json());
    }
  }

  // The at-a-glance counts of projects and contacts come from the server, counted in SQL.
  async function loadCounts() {
    const res = await fetch(`/api/clients/${clientId}/counts`);
    if (res.ok) {
      setCounts(await res.json());
    }
  }

  useEffect(() => {
    async function load() {
      const res = await fetch(
        `/api/clients/${clientId}/projects${showAll ? '?all=true' : ''}`,
      );
      if (res.ok) {
        setProjects(await res.json());
      }
    }
    void load();
  }, [clientId, showAll]);

  useEffect(() => {
    void loadContacts();
    void loadCounts();
  }, [clientId]);

  // A client has one main contact. Pick it by flagging the chosen contact primary on the server,
  // then reload so the "who is primary" line and the row buttons reflect the new choice.
  async function onPickPrimary(contactId: number) {
    const res = await fetch(
      `/api/clients/${clientId}/contacts/${contactId}/primary`,
      { method: 'POST' },
    );
    if (res.ok) {
      setContacts(await res.json());
    }
  }

  const primaryContact = contacts.find((contact) => contact.primary);

  async function onAddContact(event: FormEvent) {
    event.preventDefault();
    if (!EMAIL_RE.test(email)) {
      setEmailError(true);
      return;
    }
    setEmailError(false);
    const res = await fetch(`/api/clients/${clientId}/contacts`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email, role }),
    });
    if (res.ok) {
      setName('');
      setEmail('');
      setRole('');
      await loadContacts();
      await loadCounts();
    }
  }

  return (
    <section data-testid="client-detail">
      <dl data-testid="client-counts">
        <dt>Projects</dt>
        <dd data-testid="client-projects-count">{counts.projects}</dd>
        <dt>Contacts</dt>
        <dd data-testid="client-contacts-count">{counts.contacts}</dd>
      </dl>

      <button
        data-testid="client-statement-open"
        type="button"
        onClick={() => setStatementOpen(true)}
      >
        Statement
      </button>
      {statementOpen && <ClientStatement clientId={clientId} />}

      <h2>Projects</h2>
      <button
        data-testid="client-projects-show-all"
        type="button"
        aria-pressed={showAll}
        onClick={() => setShowAll((v) => !v)}
      >
        {showAll ? 'Show active only' : 'Show finished and hidden'}
      </button>
      <table data-testid="client-projects-table">
        <thead>
          <tr>
            <th>Name</th>
          </tr>
        </thead>
        <tbody>
          {projects.map((project) => (
            <tr key={project.id} data-testid={`project-row-${project.id}`}>
              <td data-testid="project-name">{project.name}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <h2>Contacts</h2>
      <p>
        Main contact:{' '}
        <span data-testid="client-primary-contact">{primaryContact?.name ?? ''}</span>
      </p>
      <table data-testid="client-contacts-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Role</th>
            <th>Main</th>
          </tr>
        </thead>
        <tbody>
          {contacts.map((contact) => (
            <tr key={contact.id} data-testid={`contact-row-${contact.id}`}>
              <td data-testid="contact-name">{contact.name}</td>
              <td data-testid="contact-email">{contact.email}</td>
              <td data-testid="contact-role">{contact.role}</td>
              <td>
                <button
                  data-testid={`contact-primary-${contact.id}`}
                  type="button"
                  aria-pressed={contact.primary}
                  disabled={contact.primary}
                  onClick={() => void onPickPrimary(contact.id)}
                >
                  {contact.primary ? 'Main contact' : 'Make main'}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <form data-testid="contact-form" onSubmit={onAddContact}>
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
        <input
          data-testid="contact-form-role"
          placeholder="Role"
          value={role}
          onChange={(e) => setRole(e.target.value)}
        />
        {emailError && (
          <p data-testid="contact-form-email-error">
            Enter a valid email address.
          </p>
        )}
        <button data-testid="contact-form-submit" type="submit">
          Add contact
        </button>
      </form>
    </section>
  );
}
