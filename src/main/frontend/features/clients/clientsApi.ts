// Data access + validation for the clients feature, kept in one place so the page and its forms
// share a single definition of "what a client is" and "how we talk to the server" (CLAUDE.md —
// keep each feature's code together; features own their state).
export type Client = { id: number; name: string; email: string };

// Every client needs a proper email address. Kept in sync with the server-side check in
// ClientsPostLogic and the DB CHECK constraint (V2__clients_email_check.sql).
const EMAIL_PATTERN = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

export const INVALID_EMAIL = 'Enter a valid email address.';
export const DUPLICATE_EMAIL = 'A client with this email already exists.';

// Validate an email before it reaches the server. `takenEmails` are the emails this one must not
// collide with (the server and the clients_email_unique constraint are the authoritative guards).
// Returns the error to show, or '' when the email is acceptable.
export function validateEmail(email: string, takenEmails: string[]): string {
  const trimmed = email.trim();
  if (!EMAIL_PATTERN.test(trimmed)) {
    return INVALID_EMAIL;
  }
  if (takenEmails.includes(trimmed)) {
    return DUPLICATE_EMAIL;
  }
  return '';
}

// Load the full client list. Returns null when the request fails so callers can keep what they
// already have rather than blanking the list.
export async function fetchClients(): Promise<Client[] | null> {
  const res = await fetch('/api/clients');
  return res.ok ? res.json() : null;
}

export async function createClient(input: { name: string; email: string }): Promise<Response> {
  return fetch('/api/clients', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(input),
  });
}

export async function archiveClient(id: number): Promise<Response> {
  return fetch('/api/clients/archive', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ id }),
  });
}
