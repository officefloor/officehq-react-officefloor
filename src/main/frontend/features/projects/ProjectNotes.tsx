import React, { useEffect, useState } from 'react';

// A project's notes, shown when a project is opened from the projects list. Owns its own state
// (CLAUDE.md — features own their state). Notes are listed newest first (server-ordered), and a new
// note is written through the form and lands on top.
type Note = { id: number; text: string };

export function ProjectNotes({ projectId }: { projectId: number }) {
  const [notes, setNotes] = useState<Note[]>([]);
  const [text, setText] = useState('');

  async function load() {
    const res = await fetch(`/api/projects/notes?projectId=${projectId}`);
    if (res.ok) {
      setNotes(await res.json());
    }
  }

  useEffect(() => {
    void load();
  }, [projectId]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!text.trim()) {
      return;
    }
    const res = await fetch('/api/projects/notes', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ projectId, text }),
    });
    if (res.ok) {
      setNotes(await res.json());
      setText('');
    }
  }

  return (
    <section data-testid="project-notes">
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
      <ul data-testid="project-notes-list">
        {notes.map((n) => (
          <li key={n.id} data-testid={`note-row-${n.id}`}>
            <span data-testid="note-text">{n.text}</span>
          </li>
        ))}
      </ul>
    </section>
  );
}
