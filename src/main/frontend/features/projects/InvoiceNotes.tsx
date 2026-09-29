import React, { useEffect, useState } from 'react';

// An invoice's notes, shown when an invoice is opened from the project's invoice list. Owns its own
// state (CLAUDE.md — features own their state). Notes are listed newest first (server-ordered), and a
// new note is written through the form and lands on top.
type Note = { id: number; text: string };

export function InvoiceNotes({ invoiceId }: { invoiceId: number }) {
  const [notes, setNotes] = useState<Note[]>([]);
  const [text, setText] = useState('');

  async function load() {
    const res = await fetch(`/api/invoices/notes?invoiceId=${invoiceId}`);
    if (res.ok) {
      setNotes(await res.json());
    }
  }

  useEffect(() => {
    void load();
  }, [invoiceId]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!text.trim()) {
      return;
    }
    const res = await fetch('/api/invoices/notes', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ invoiceId, text }),
    });
    if (res.ok) {
      setNotes(await res.json());
      setText('');
    }
  }

  return (
    <section data-testid="invoice-notes">
      <h3>Notes</h3>
      <form data-testid="note-form" onSubmit={onSubmit}>
        <input
          data-testid="note-form-text"
          placeholder="Write a note"
          value={text}
          onChange={(e) => setText(e.target.value)}
        />
        <button type="submit" data-testid="note-form-submit">
          Add note
        </button>
      </form>
      <ul data-testid="invoice-notes-list">
        {notes.map((n) => (
          <li key={n.id} data-testid={`note-row-${n.id}`}>
            <span data-testid="note-text">{n.text}</span>
          </li>
        ))}
      </ul>
    </section>
  );
}
