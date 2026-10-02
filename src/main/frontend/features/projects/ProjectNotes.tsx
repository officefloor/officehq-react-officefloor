import React, { useEffect, useState } from 'react';

// A project's notes, rendered inside the projects feature when a project is opened. Lists the notes
// newest first and lets the owner write another, which the server stamps "now" so it lands on top.
// Owns its own state and data loading (no global store); composed, not branched.
type Note = {
  id: number;
  targetType: string;
  targetId: number;
  text: string;
  at: string;
};

export function ProjectNotes({ projectId }: { projectId: number }) {
  const [notes, setNotes] = useState<Note[]>([]);
  const [text, setText] = useState('');

  async function load() {
    const res = await fetch(`/api/projects/${projectId}/notes`);
    setNotes(await res.json());
  }

  useEffect(() => {
    void load();
  }, [projectId]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!text.trim()) {
      return;
    }
    const res = await fetch(`/api/projects/${projectId}/notes`, {
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
