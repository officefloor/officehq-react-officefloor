import React, { useEffect, useState } from 'react';

// An invoice's notes, rendered inside the opened invoice within the projects feature. Lists the
// notes newest first and lets the owner write another, which the server stamps "now" so it lands on
// top. Mirrors the project's notes surface for invoices. Owns its own state and data loading (no
// global store); composed, not branched.
type Note = {
  id: number;
  targetType: string;
  targetId: number;
  text: string;
  at: string;
};

export function InvoiceNotes({ invoiceId }: { invoiceId: number }) {
  const [notes, setNotes] = useState<Note[]>([]);
  const [text, setText] = useState('');

  async function load() {
    const res = await fetch(`/api/invoices/${invoiceId}/notes`);
    setNotes(await res.json());
  }

  useEffect(() => {
    void load();
  }, [invoiceId]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!text.trim()) {
      return;
    }
    const res = await fetch(`/api/invoices/${invoiceId}/notes`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ text }),
    });
    if (!res.ok) {
      return;
    }
    setText('');
    await load();
  }

  return (
    <section data-testid="invoice-notes">
      <h2>Notes</h2>

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
