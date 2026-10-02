import React, { useEffect, useState } from 'react';

// A project's tags (chips), rendered inside the projects feature when a project is opened. Shows the
// tags currently attached, lets the owner add another from the tags not yet on the project, and
// remove one. Owns its own state and data loading (no global store); composed, not branched.
type Tag = { id: number; name: string };

export function ProjectTags({ projectId }: { projectId: number }) {
  const [tags, setTags] = useState<Tag[]>([]);
  const [allTags, setAllTags] = useState<Tag[]>([]);
  const [addTagId, setAddTagId] = useState('');

  async function load() {
    const [attachedRes, allRes] = await Promise.all([
      fetch(`/api/projects/${projectId}/tags`),
      fetch('/api/tags'),
    ]);
    setTags(await attachedRes.json());
    setAllTags(await allRes.json());
  }

  useEffect(() => {
    void load();
  }, [projectId]);

  // Only tags not already on the project can be added.
  const available = allTags.filter((t) => !tags.some((a) => a.id === t.id));

  async function onAdd(e: React.FormEvent) {
    e.preventDefault();
    if (!addTagId) {
      return;
    }
    const res = await fetch(`/api/projects/${projectId}/tags`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ tagId: Number(addTagId) }),
    });
    if (!res.ok) {
      return;
    }
    setAddTagId('');
    await load();
  }

  async function remove(tagId: number) {
    const res = await fetch(`/api/projects/${projectId}/tags/${tagId}`, { method: 'DELETE' });
    if (!res.ok) {
      return;
    }
    await load();
  }

  return (
    <section data-testid="project-tags">
      <h2>Tags</h2>

      <ul data-testid="project-tags-list">
        {tags.map((t) => (
          <li key={t.id} data-testid={`project-tag-row-${t.id}`}>
            <span data-testid={`project-tag-${t.id}`}>{t.name}</span>
            <button
              type="button"
              data-testid={`project-tag-remove-${t.id}`}
              onClick={() => void remove(t.id)}
            >
              Remove
            </button>
          </li>
        ))}
      </ul>

      <form data-testid="project-tag-add-form" onSubmit={onAdd}>
        <select
          data-testid="project-tag-add"
          value={addTagId}
          onChange={(e) => setAddTagId(e.target.value)}
        >
          <option value="">Select a tag</option>
          {available.map((t) => (
            <option key={t.id} value={t.id}>
              {t.name}
            </option>
          ))}
        </select>
        <button type="submit" data-testid="project-tag-add-submit">
          Add tag
        </button>
      </form>
    </section>
  );
}
