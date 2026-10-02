import { useEffect, useState, type FormEvent } from 'react';
import { ClientStatement } from './ClientStatement';
import { formatMoney } from '../../ui/money';

// Opened from the clients list: a client's detail view lists the projects done for them, scoped to
// the client via /api/clients/<id>/projects. Reuses the project-row-<id>/project-name anchors so the
// listing reads the same way wherever projects appear. It also keeps the client's contacts (name,
// email, role), scoped the same way via /api/clients/<id>/contacts, with a form to add one.
type Project = { id: number; name: string };
type Contact = { id: number; name: string; email: string; role: string; primary: boolean };
type Counts = { projects: number; contacts: number };
// An open invoice the client still owes on — a candidate to put part of a lump payment against.
type OpenInvoice = { id: number; projectName: string; amount: number; due: number };

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
  // Recording a lump payment split across the client's open invoices: whether the form is open, the
  // lump sum, the date it was paid, the open invoices to split across, and each invoice's share.
  const [paymentOpen, setPaymentOpen] = useState(false);
  const [openInvoices, setOpenInvoices] = useState<OpenInvoice[]>([]);
  const [paymentAmount, setPaymentAmount] = useState('');
  const [paymentDate, setPaymentDate] = useState('');
  const [allocations, setAllocations] = useState<Record<number, string>>({});
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

  // Open the lump-payment form, loading the client's open invoices (those with something still due)
  // from their statement so each can be given a share of the payment.
  async function onOpenPayment() {
    setPaymentOpen(true);
    const res = await fetch(`/api/clients/${clientId}/statement`);
    if (res.ok) {
      const statement = (await res.json()) as { invoices: OpenInvoice[] };
      setOpenInvoices(statement.invoices.filter((invoice) => Number(invoice.due) > 0));
    }
  }

  // Split the lump payment across the invoices: send each invoice's share, then close the form.
  async function onRecordPayment(event: FormEvent) {
    event.preventDefault();
    const splits = openInvoices
      .map((invoice) => ({ invoiceId: invoice.id, amount: Number(allocations[invoice.id] ?? '') }))
      .filter((split) => split.amount > 0);
    const res = await fetch(`/api/clients/${clientId}/payments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ amount: Number(paymentAmount), date: paymentDate, allocations: splits }),
    });
    if (res.ok) {
      setPaymentOpen(false);
      setPaymentAmount('');
      setPaymentDate('');
      setAllocations({});
    }
  }

  return (
    <section data-testid="client-detail">
      <dl data-testid="client-counts">
        <dt>Jobs</dt>
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

      <button
        data-testid="client-record-payment"
        type="button"
        onClick={() => void onOpenPayment()}
      >
        Record payment
      </button>
      {paymentOpen && (
        <form data-testid="payment-form" onSubmit={onRecordPayment}>
          <input
            data-testid="payment-form-amount"
            placeholder="Amount"
            value={paymentAmount}
            onChange={(e) => setPaymentAmount(e.target.value)}
          />
          <input
            data-testid="payment-form-date"
            placeholder="Date"
            value={paymentDate}
            onChange={(e) => setPaymentDate(e.target.value)}
          />
          <table data-testid="payment-alloc-table">
            <thead>
              <tr>
                <th>Invoice</th>
                <th>Job</th>
                <th>Left to pay</th>
                <th>Apply</th>
              </tr>
            </thead>
            <tbody>
              {openInvoices.map((invoice) => (
                <tr key={invoice.id} data-testid={`payment-alloc-row-${invoice.id}`}>
                  <td>{invoice.id}</td>
                  <td>{invoice.projectName}</td>
                  <td>{formatMoney(invoice.due)}</td>
                  <td>
                    <input
                      data-testid={`payment-alloc-${invoice.id}`}
                      placeholder="0"
                      value={allocations[invoice.id] ?? ''}
                      onChange={(e) =>
                        setAllocations((prev) => ({ ...prev, [invoice.id]: e.target.value }))
                      }
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <button data-testid="payment-form-submit" type="submit">
            Record payment
          </button>
        </form>
      )}

      <h2>Jobs</h2>
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
