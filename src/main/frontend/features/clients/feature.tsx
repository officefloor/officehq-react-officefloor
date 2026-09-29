import React, { useEffect, useState } from 'react';
import type { Feature } from '../../router/routes';

// Clients feature: add a client (name + email) and list every client. Opening a client
// (client-open-<id>) reveals the projects being done for them — the client's own projects listing,
// scoped via its /api/clients/<id>/projects endpoint. Owns its own state; talks to its own
// /api/clients endpoints. data-testid anchors follow the spec's conventions.
type Client = { id: number; name: string; email: string };
type Project = { id: number; name: string; clientId: number; clientName: string };
type Contact = { id: number; clientId: number; name: string; email: string; role: string };
type ClientSummary = { projectCount: number; contactCount: number };
type StatementInvoice = { id: number; projectId: number; amountDue: number };
type ClientStatement = { invoices: StatementInvoice[]; outstandingTotal: number };

// Money, the way every invoice figure is shown across the app: a dollar sign and two decimals.
function money(amount: number): string {
  return `$${Number(amount).toFixed(2)}`;
}

// A client's statement: all of their invoices in one place, each with what is still owed on it, and
// the total still owed across them all. Kept behind an opener so the client detail stays compact.
// Owns its own statement state, scoped to the one client it is showing; talks to that client's own
// /api/clients/<id>/statement endpoint.
function ClientStatement({ client }: { client: Client }) {
  const [statement, setStatement] = useState<ClientStatement | null>(null);
  const [open, setOpen] = useState(false);

  useEffect(() => {
    async function load() {
      const res = await fetch(`/api/clients/${client.id}/statement`);
      setStatement(await res.json());
    }
    void load();
  }, [client.id]);

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
  const [summary, setSummary] = useState<ClientSummary>({ projectCount: 0, contactCount: 0 });

  useEffect(() => {
    async function load() {
      const res = await fetch(`/api/clients/${client.id}/summary`);
      setSummary(await res.json());
    }
    void load();
  }, [client.id]);

  return (
    <section data-testid="client-summary">
      <span data-testid="client-projects-count">{summary.projectCount}</span>
      <span data-testid="client-contacts-count">{summary.contactCount}</span>
    </section>
  );
}

// A client's contacts: the people the user keeps for them (name, email, role), with a form to add
// another. Owns its own contact state, scoped to the one client it is showing; talks to that
// client's own /api/clients/<id>/contacts endpoints.
function ClientContacts({ client }: { client: Client }) {
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [role, setRole] = useState('');
  const [emailError, setEmailError] = useState('');

  async function load() {
    const res = await fetch(`/api/clients/${client.id}/contacts`);
    setContacts(await res.json());
  }

  useEffect(() => {
    void load();
  }, [client.id]);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!name.trim() || !email.trim() || !role.trim()) {
      return;
    }
    // A contact needs a proper email too; same shape the server enforces (see CreateContact).
    if (!EMAIL_RE.test(email.trim())) {
      setEmailError('Enter a valid email address.');
      return;
    }
    setEmailError('');
    await fetch(`/api/clients/${client.id}/contacts`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email, role }),
    });
    setName('');
    setEmail('');
    setRole('');
    await load();
  }

  return (
    <section data-testid="client-contacts">
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

      <table data-testid="client-contacts-table">
        <tbody>
          {contacts.map((ct) => (
            <tr key={ct.id} data-testid={`contact-row-${ct.id}`}>
              <td data-testid="contact-name">{ct.name}</td>
              <td data-testid="contact-email">{ct.email}</td>
              <td data-testid="contact-role">{ct.role}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}

// A client's detail: the projects being done for them. Owns its own project state, scoped to the one
// client it is showing. Reuses the project-row-<id>/project-name anchors in a client-scoped table.
// Shows only the client's ACTIVE projects by default (scope=active); the show-all toggle asks the
// server for everything (scope=all), so the finished and hidden (archived) ones appear too.
function ClientProjects({ client }: { client: Client }) {
  const [projects, setProjects] = useState<Project[]>([]);
  const [showAll, setShowAll] = useState(false);

  useEffect(() => {
    async function load() {
      const scope = showAll ? 'all' : 'active';
      const res = await fetch(`/api/clients/${client.id}/projects?scope=${scope}`);
      setProjects(await res.json());
    }
    void load();
  }, [client.id, showAll]);

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

// Every client needs a proper email. Same shape the server enforces (see CreateClient) so the UI
// and the API agree on what "valid" means.
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function ClientsPage() {
  const [clients, setClients] = useState<Client[]>([]);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [emailError, setEmailError] = useState('');
  // Client-side name filter: the list can get long, so a search box narrows it. Case-insensitive
  // substring match on the client name; an empty box shows everyone.
  const [search, setSearch] = useState('');
  const [openId, setOpenId] = useState<number | null>(null);

  async function load() {
    const res = await fetch('/api/clients');
    setClients(await res.json());
  }

  useEffect(() => {
    void load();
  }, []);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!EMAIL_RE.test(email.trim())) {
      setEmailError('Enter a valid email address.');
      return;
    }
    setEmailError('');
    await fetch('/api/clients', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email }),
    });
    setName('');
    setEmail('');
    await load();
  }

  // Tuck a client away: archived clients are retained server-side but drop off this list and out of
  // the search. Reloading after archiving is enough — the /api/clients list excludes them.
  async function archive(id: number) {
    await fetch(`/api/clients/${id}/archive`, { method: 'POST' });
    if (openId === id) {
      setOpenId(null);
    }
    await load();
  }

  const visible = clients.filter((c) =>
    c.name.toLowerCase().includes(search.trim().toLowerCase()),
  );

  const open = clients.find((c) => c.id === openId) ?? null;

  return (
    <section data-testid="clients">
      <input
        data-testid="client-search"
        placeholder="Search clients by name"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
      />
      <form data-testid="client-form" onSubmit={submit}>
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
          <p data-testid="client-form-email-error" role="alert">
            {emailError}
          </p>
        )}
        <button data-testid="client-form-submit" type="submit">
          Add client
        </button>
      </form>

      {clients.length === 0 ? (
        <p data-testid="clients-empty">No clients yet.</p>
      ) : (
        <table data-testid="clients-table">
          <tbody>
            {visible.map((c) => (
              <tr key={c.id} data-testid={`client-row-${c.id}`}>
                <td data-testid="client-name">{c.name}</td>
                <td data-testid="client-email">{c.email}</td>
                <td>
                  <button data-testid={`client-open-${c.id}`} onClick={() => setOpenId(c.id)}>
                    Open
                  </button>
                  <button data-testid={`client-archive-${c.id}`} onClick={() => archive(c.id)}>
                    Archive
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {open && <ClientSummaryBadges key={`summary-${open.id}`} client={open} />}
      {open && <ClientStatement key={`statement-${open.id}`} client={open} />}
      {open && <ClientContacts key={`contacts-${open.id}`} client={open} />}
      {open && <ClientProjects key={open.id} client={open} />}
    </section>
  );
}

export const feature: Feature = { id: 'clients', label: 'Clients', Page: ClientsPage };
