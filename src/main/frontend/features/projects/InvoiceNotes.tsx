import { useEffect, useState, type FormEvent } from 'react';

// A user writes free-text notes about an invoice, just as they can about a project. This panel shows
// the invoice's notes newest first and has a form to add one on top. Notes are scoped to the invoice
// via /api/invoices/<id>/notes; a newly written note is stamped on the server and comes back at the
// head of the list.
type Note = { id: number; text: string; at: string };

export function InvoiceNotes({ invoiceId }: { invoiceId: number }) {
  const [notes, setNotes] = useState<Note[]>([]);
  const [text, setText] = useState('');

  async function loadNotes() {
    const res = await fetch(`/api/invoices/${invoiceId}/notes`);
    if (res.ok) {
      setNotes(await res.json());
    }
  }

  useEffect(() => {
    void loadNotes();
  }, [invoiceId]);

  async function onAdd(event: FormEvent) {
    event.preventDefault();
    if (!text.trim()) {
      return;
    }
    const res = await fetch(`/api/invoices/${invoiceId}/notes`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ text }),
    });
    if (res.ok) {
      setText('');
      setNotes(await res.json());
    }
  }

  return (
    <section data-testid="invoice-notes">
      <h2>Notes</h2>

      <form data-testid="note-form" onSubmit={onAdd}>
        <input
          data-testid="note-form-text"
          placeholder="Write a note"
          value={text}
          onChange={(e) => setText(e.target.value)}
        />
        <button data-testid="note-form-submit" type="submit">
          Add note
        </button>
      </form>

      <ul data-testid="invoice-notes-list">
        {notes.map((note) => (
          <li key={note.id} data-testid={`note-row-${note.id}`}>
            <span data-testid="note-text">{note.text}</span>
          </li>
        ))}
      </ul>
    </section>
  );
}
