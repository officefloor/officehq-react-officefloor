import React, { useCallback, useEffect, useState } from 'react';
import type { Feature } from '../../router/routes';

// Clients feature: add a client (name + email) and list every client. Opening a client
// (client-open-<id>) reveals the projects being done for them — the client's own projects listing,
// scoped via its /api/clients/<id>/projects endpoint. Owns its own state; talks to its own
// /api/clients endpoints. data-testid anchors follow the spec's conventions.
type Client = { id: number; name: string; email: string };
type Project = { id: number; name: string; clientId: number; clientName: string };
type Contact = {
  id: number;
  clientId: number;
  name: string;
  email: string;
  role: string;
  primary: boolean;
};
type ClientSummary = { projectCount: number; contactCount: number };
type StatementInvoice = { id: number; projectId: number; amountDue: number };
type ClientStatement = { invoices: StatementInvoice[]; outstandingTotal: number };

// Money, the way every invoice figure is shown across the app: a dollar sign and two decimals.
function money(amount: number): string {
  return `$${Number(amount).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
}

// Every client (and contact) needs a proper email. Same shape the server enforces (see CreateClient
// / CreateContact) so the UI and the API agree on what "valid" means.
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
function isValidEmail(value: string): boolean {
  return EMAIL_RE.test(value.trim());
}

// Load a JSON resource from `url` into state, refetching whenever the url changes, and hand back a
// `reload` for callers that mutate the resource and need to pull the fresh version. Every client
// panel reads one url this way, so this is where that fetch-into-state pattern lives once.
function useJsonResource<T>(url: string, initial: T): [T, () => Promise<void>] {
  const [data, setData] = useState<T>(initial);
  const reload = useCallback(async () => {
    const res = await fetch(url);
    setData(await res.json());
  }, [url]);
  useEffect(() => {
    void reload();
  }, [reload]);
  return [data, reload];
}

// A client's statement: all of their invoices in one place, each with what is still owed on it, and
// the total still owed across them all. Kept behind an opener so the client detail stays compact.
// Owns its own statement state, scoped to the one client it is showing; talks to that client's own
// /api/clients/<id>/statement endpoint.
function ClientStatement({ client }: { client: Client }) {
  const [statement] = useJsonResource<ClientStatement | null>(
    `/api/clients/${client.id}/statement`,
    null,
  );
  const [open, setOpen] = useState(false);

  return (
    <section data-testid="client-statement">
      <button data-testid="client-statement-open" onClick={() => setOpen(true)}>
        Statement
      </button>
      {open && statement && (
        <>
          <table data-testid="client-statement-table">
            <tbody>
              {statement.invoices.map((inv) => (
                <tr key={inv.id} data-testid={`statement-invoice-row-${inv.id}`}>
                  <td data-testid="statement-invoice-due">{money(inv.amountDue)}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <p data-testid="client-outstanding-total">{money(statement.outstandingTotal)}</p>
        </>
      )}
    </section>
  );
}

// A client's at-a-glance counts: how many projects and contacts are kept for them. Owns its own
// summary state, scoped to the one client it is showing; talks to that client's own
// /api/clients/<id>/summary endpoint.
function ClientSummaryBadges({ client }: { client: Client }) {
  const [summary] = useJsonResource<ClientSummary>(`/api/clients/${client.id}/summary`, {
    projectCount: 0,
    contactCount: 0,
  });

  return (
    <section data-testid="client-summary">
      <span data-testid="client-projects-count">{summary.projectCount}</span>
      <span data-testid="client-contacts-count">{summary.contactCount}</span>
    </section>
  );
}

// The add-a-contact form: name, email and role, all required, with the same email check the server
// runs. Owns only its own field state and clears itself once the parent has taken the new contact.
function ContactForm({
  onAdd,
}: {
  onAdd: (contact: { name: string; email: string; role: string }) => Promise<void>;
}) {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [role, setRole] = useState('');
  const [emailError, setEmailError] = useState('');

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!name.trim() || !email.trim() || !role.trim()) {
      return;
    }
    if (!isValidEmail(email)) {
      setEmailError('Enter a valid email address.');
      return;
    }
    setEmailError('');
    await onAdd({ name, email, role });
    setName('');
    setEmail('');
    setRole('');
  }

  return (
    <form data-testid="contact-form" onSubmit={submit}>
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
      <button data-testid="contact-form-submit" type="submit">
        Add contact
      </button>
    </form>
  );
}

// The contacts list itself: a row per contact with a control to promote it to the client's primary.
function ContactsTable({
  contacts,
  onMakePrimary,
}: {
  contacts: Contact[];
  onMakePrimary: (id: number) => void;
}) {
  return (
    <table data-testid="client-contacts-table">
      <tbody>
        {contacts.map((ct) => (
          <tr key={ct.id} data-testid={`contact-row-${ct.id}`}>
            <td data-testid="contact-name">{ct.name}</td>
            <td data-testid="contact-email">{ct.email}</td>
            <td data-testid="contact-role">{ct.role}</td>
            <td>
              <button data-testid={`contact-primary-${ct.id}`} onClick={() => onMakePrimary(ct.id)}>
                Make primary
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

// A client's contacts: the people the user keeps for them (name, email, role), with a form to add
// another. Owns its own contact state, scoped to the one client it is showing; talks to that
// client's own /api/clients/<id>/contacts endpoints. The form and the table are their own pieces so
// this stays a thin coordinator: load, add, promote, show who is primary.
function ClientContacts({ client }: { client: Client }) {
  const [contacts, reload] = useJsonResource<Contact[]>(`/api/clients/${client.id}/contacts`, []);

  async function addContact(contact: { name: string; email: string; role: string }) {
    await fetch(`/api/clients/${client.id}/contacts`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(contact),
    });
    await reload();
  }

  // Pick this contact as the client's one main contact, then refresh so the new primary shows.
  async function makePrimary(id: number) {
    await fetch(`/api/contacts/${id}/primary`, { method: 'POST' });
    await reload();
  }

  const primary = contacts.find((ct) => ct.primary) ?? null;

  return (
    <section data-testid="client-contacts">
      <p data-testid="client-primary-contact">{primary ? primary.name : ''}</p>
      <ContactForm onAdd={addContact} />
      <ContactsTable contacts={contacts} onMakePrimary={makePrimary} />
    </section>
  );
}

// A client's detail: the projects being done for them. Owns its own project state, scoped to the one
// client it is showing. Reuses the project-row-<id>/project-name anchors in a client-scoped table.
// Shows only the client's ACTIVE projects by default (scope=active); the show-all toggle asks the
// server for everything (scope=all), so the finished and hidden (archived) ones appear too.
function ClientProjects({ client }: { client: Client }) {
  const [showAll, setShowAll] = useState(false);
  const [projects] = useJsonResource<Project[]>(
    `/api/clients/${client.id}/projects?scope=${showAll ? 'all' : 'active'}`,
    [],
  );

  return (
    <section data-testid="client-projects">
      <button data-testid="client-projects-show-all" onClick={() => setShowAll((v) => !v)}>
        {showAll ? 'Show only active' : 'Show finished and hidden too'}
      </button>
      <table data-testid="client-projects-table">
        <tbody>
          {projects.map((p) => (
            <tr key={p.id} data-testid={`project-row-${p.id}`}>
              <td data-testid="project-name">{p.name}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}

// A name-and-email form for a client, used to add one and reusable for correcting one. `testid` is
// the anchor prefix (its fields are `${testid}-name` / `-email` / `-email-error` / `-submit`), so a
// second instance (e.g. an edit form) just passes a different prefix. Validates the email format the
// way the server does; `onSubmit` returns an error string to show (e.g. a duplicate the server
// rejected) or null on success, in which case the form clears back to its initial values.
function ClientForm({
  testid,
  submitLabel,
  initial = { name: '', email: '' },
  onSubmit,
}: {
  testid: string;
  submitLabel: string;
  initial?: { name: string; email: string };
  onSubmit: (values: { name: string; email: string }) => Promise<string | null>;
}) {
  const [name, setName] = useState(initial.name);
  const [email, setEmail] = useState(initial.email);
  const [emailError, setEmailError] = useState('');

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!isValidEmail(email)) {
      setEmailError('Enter a valid email address.');
      return;
    }
    const error = await onSubmit({ name, email });
    if (error) {
      setEmailError(error);
      return;
    }
    setEmailError('');
    setName(initial.name);
    setEmail(initial.email);
  }

  return (
    <form data-testid={testid} onSubmit={submit}>
      <input
        data-testid={`${testid}-name`}
        placeholder="Name"
        value={name}
        onChange={(e) => setName(e.target.value)}
      />
      <input
        data-testid={`${testid}-email`}
        placeholder="Email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
      />
      {emailError && (
        <p data-testid={`${testid}-email-error`} role="alert">
          {emailError}
        </p>
      )}
      <button data-testid={`${testid}-submit`} type="submit">
        {submitLabel}
      </button>
    </form>
  );
}

// The client list: a row per client with controls to open its detail or archive it.
function ClientsTable({
  clients,
  onOpen,
  onEdit,
  onArchive,
}: {
  clients: Client[];
  onOpen: (id: number) => void;
  onEdit: (id: number) => void;
  onArchive: (id: number) => void;
}) {
  return (
    <table data-testid="clients-table">
      <tbody>
        {clients.map((c) => (
          <tr key={c.id} data-testid={`client-row-${c.id}`}>
            <td data-testid="client-name">{c.name}</td>
            <td data-testid="client-email">{c.email}</td>
            <td>
              <button data-testid={`client-open-${c.id}`} onClick={() => onOpen(c.id)}>
                Open
              </button>
              <button data-testid={`client-edit-${c.id}`} onClick={() => onEdit(c.id)}>
                Edit
              </button>
              <button data-testid={`client-archive-${c.id}`} onClick={() => onArchive(c.id)}>
                Archive
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

function ClientsPage() {
  const [clients, reload] = useJsonResource<Client[]>('/api/clients', []);
  // Client-side name filter: the list can get long, so a search box narrows it. Case-insensitive
  // substring match on the client name; an empty box shows everyone.
  const [search, setSearch] = useState('');
  const [openId, setOpenId] = useState<number | null>(null);
  // Which client (if any) is being corrected: opening the edit form seeds it with that client's
  // current name and email so the user tweaks rather than retypes.
  const [editingId, setEditingId] = useState<number | null>(null);

  // Two clients cannot share an email; the server rejects a duplicate, so surface it and keep the
  // form as-is rather than clearing or reloading (nothing was added).
  async function addClient(values: { name: string; email: string }): Promise<string | null> {
    const res = await fetch('/api/clients', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(values),
    });
    if (!res.ok) {
      return 'A client with this email already exists.';
    }
    await reload();
    return null;
  }

  // Correct a client's name or email. The server rejects a duplicate email (against another
  // client), so surface it and keep the form open; on success reload and close the edit form.
  async function editClient(
    id: number,
    values: { name: string; email: string },
  ): Promise<string | null> {
    const res = await fetch(`/api/clients/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(values),
    });
    if (!res.ok) {
      return 'A client with this email already exists.';
    }
    await reload();
    setEditingId(null);
    return null;
  }

  // Tuck a client away: archived clients are retained server-side but drop off this list and out of
  // the search. Reloading after archiving is enough — the /api/clients list excludes them.
  async function archive(id: number) {
    await fetch(`/api/clients/${id}/archive`, { method: 'POST' });
    if (openId === id) {
      setOpenId(null);
    }
    if (editingId === id) {
      setEditingId(null);
    }
    await reload();
  }

  const term = search.trim().toLowerCase();
  const visible = clients.filter((c) => c.name.toLowerCase().includes(term));
  const open = clients.find((c) => c.id === openId) ?? null;
  const editing = clients.find((c) => c.id === editingId) ?? null;

  return (
    <section data-testid="clients">
      <input
        data-testid="client-search"
        placeholder="Search clients by name"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
      />
      <ClientForm testid="client-form" submitLabel="Add client" onSubmit={addClient} />

      {clients.length === 0 ? (
        <p data-testid="clients-empty">No clients yet.</p>
      ) : (
        <ClientsTable
          clients={visible}
          onOpen={setOpenId}
          onEdit={setEditingId}
          onArchive={archive}
        />
      )}

      {editing && (
        <ClientForm
          key={`edit-${editing.id}`}
          testid="client-edit-form"
          submitLabel="Save changes"
          initial={{ name: editing.name, email: editing.email }}
          onSubmit={(values) => editClient(editing.id, values)}
        />
      )}

      {open && <ClientSummaryBadges key={`summary-${open.id}`} client={open} />}
      {open && <ClientStatement key={`statement-${open.id}`} client={open} />}
      {open && <ClientContacts key={`contacts-${open.id}`} client={open} />}
      {open && <ClientProjects key={open.id} client={open} />}
    </section>
  );
}

export const feature: Feature = { id: 'clients', label: 'Clients', Page: ClientsPage };
