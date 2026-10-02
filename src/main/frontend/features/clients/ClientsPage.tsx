import { useEffect, useState, type FormEvent } from 'react';
import { ClientDetail } from './ClientDetail';

// This arm's convention: the page component owns the feature's state, data loading and layout.
type Client = { id: number; name: string; email: string };

// A client must carry a proper email address. Keep it simple and local to the feature: a single
// non-whitespace local part, an @, and a dotted domain.
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function ClientsPage() {
  const [clients, setClients] = useState<Client[]>([]);
  const [search, setSearch] = useState('');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [emailError, setEmailError] = useState<string | null>(null);
  const [openClientId, setOpenClientId] = useState<number | null>(null);

  // Correcting a client: the row being edited, its draft name/email, and any email error.
  const [editClientId, setEditClientId] = useState<number | null>(null);
  const [editName, setEditName] = useState('');
  const [editEmail, setEditEmail] = useState('');
  const [editEmailError, setEditEmailError] = useState<string | null>(null);

  async function load() {
    const res = await fetch(`/api/clients?q=${encodeURIComponent(search)}`);
    if (res.ok) {
      setClients(await res.json());
    }
  }

  // Reload whenever the search term changes — the server filters by name (case-insensitive).
  useEffect(() => {
    void load();
  }, [search]);

  // Archiving tucks a client away: the row is kept but drops off the list and search. Close the
  // detail pane if it was open on the archived client, then reload the (now narrower) list.
  async function onArchive(id: number) {
    const res = await fetch(`/api/clients/${id}/archive`, { method: 'POST' });
    if (res.ok) {
      if (openClientId === id) {
        setOpenClientId(null);
      }
      await load();
    }
  }

  // Open the edit form for a client, pre-filled with its current name and email.
  function onEdit(client: Client) {
    setEditClientId(client.id);
    setEditName(client.name);
    setEditEmail(client.email);
    setEditEmailError(null);
  }

  // Save a corrected name/email. The server rejects an email already used by another client.
  async function onEditSubmit(event: FormEvent) {
    event.preventDefault();
    if (editClientId === null) {
      return;
    }
    if (!EMAIL_RE.test(editEmail)) {
      setEditEmailError('Enter a valid email address.');
      return;
    }
    setEditEmailError(null);
    const res = await fetch(`/api/clients/${editClientId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: editName, email: editEmail }),
    });
    if (res.ok) {
      const result = (await res.json()) as { emailInUse?: boolean };
      if (result.emailInUse) {
        setEditEmailError('That email is already in use.');
        return;
      }
      setEditClientId(null);
      setEditEmailError(null);
      await load();
    }
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    if (!EMAIL_RE.test(email)) {
      setEmailError('Enter a valid email address.');
      return;
    }
    setEmailError(null);
    const res = await fetch('/api/clients', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email }),
    });
    if (res.ok) {
      // Two clients can't share an email: the server rejects the duplicate and adds no row.
      const result = (await res.json()) as { emailInUse?: boolean };
      if (result.emailInUse) {
        setEmailError('That email is already in use.');
        return;
      }
      setName('');
      setEmail('');
      setEmailError(null);
      await load();
    }
  }

  return (
    <section data-testid="clients-page">
      <h1>Clients</h1>

      <input
        data-testid="client-search"
        placeholder="Search clients by name"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
      />

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
          <p data-testid="client-form-email-error">{emailError}</p>
        )}
        <button data-testid="client-form-submit" type="submit">
          Add client
        </button>
      </form>

      {clients.length === 0 ? (
        <p data-testid="clients-empty">No clients yet.</p>
      ) : (
        <table data-testid="clients-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {clients.map((client) => (
              <tr key={client.id} data-testid={`client-row-${client.id}`}>
                <td data-testid="client-name">{client.name}</td>
                <td data-testid="client-email">{client.email}</td>
                <td>
                  <button
                    data-testid={`client-open-${client.id}`}
                    type="button"
                    onClick={() => setOpenClientId(client.id)}
                  >
                    Open
                  </button>
                  <button
                    data-testid={`client-edit-${client.id}`}
                    type="button"
                    onClick={() => onEdit(client)}
                  >
                    Edit
                  </button>
                  <button
                    data-testid={`client-archive-${client.id}`}
                    type="button"
                    onClick={() => void onArchive(client.id)}
                  >
                    Archive
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {editClientId !== null && (
        <form data-testid="client-edit-form" onSubmit={onEditSubmit}>
          <input
            data-testid="client-edit-form-name"
            placeholder="Name"
            value={editName}
            onChange={(e) => setEditName(e.target.value)}
          />
          <input
            data-testid="client-edit-form-email"
            placeholder="Email"
            value={editEmail}
            onChange={(e) => setEditEmail(e.target.value)}
          />
          {editEmailError && (
            <p data-testid="client-edit-form-email-error">{editEmailError}</p>
          )}
          <button data-testid="client-edit-form-submit" type="submit">
            Save changes
          </button>
        </form>
      )}

      {openClientId !== null && <ClientDetail clientId={openClientId} />}
    </section>
  );
}
