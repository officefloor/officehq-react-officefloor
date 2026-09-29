import React, { useEffect, useState } from 'react';

// A project's tags (labels the owner uses to group projects), shown as chips when a project is
// opened from the projects list. Owns its own state (CLAUDE.md — features own their state). Tags
// are picked from the shared catalogue and added / removed one at a time.
type Tag = { id: number; name: string };

export function ProjectTags({ projectId }: { projectId: number }) {
  const [tags, setTags] = useState<Tag[]>([]);
  const [catalogue, setCatalogue] = useState<Tag[]>([]);
  const [selected, setSelected] = useState('');

  async function loadTags() {
    const res = await fetch(`/api/projects/tags?projectId=${projectId}`);
    if (res.ok) {
      setTags(await res.json());
    }
  }

  async function loadCatalogue() {
    const res = await fetch('/api/tags');
    if (res.ok) {
      const list: Tag[] = await res.json();
      setCatalogue(list);
      setSelected((prev) => prev || (list[0] ? String(list[0].id) : ''));
    }
  }

  useEffect(() => {
    void loadTags();
  }, [projectId]);

  useEffect(() => {
    void loadCatalogue();
  }, []);

  async function onAdd(e: React.FormEvent) {
    e.preventDefault();
    if (!selected) {
      return;
    }
    const res = await fetch('/api/projects/tags', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ projectId, tagId: Number(selected) }),
    });
    if (res.ok) {
      setTags(await res.json());
    }
  }

  async function onRemove(tagId: number) {
    const res = await fetch('/api/projects/tags/remove', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ projectId, tagId }),
    });
    if (res.ok) {
      setTags(await res.json());
    }
  }

  return (
    <section data-testid="project-tags">
      <h2>Tags</h2>
      <ul data-testid="project-tags-list">
        {tags.map((t) => (
          <li key={t.id}>
            <span data-testid={`project-tag-${t.id}`}>{t.name}</span>
            <button
              type="button"
              data-testid={`project-tag-remove-${t.id}`}
              onClick={() => void onRemove(t.id)}
            >
              Remove
            </button>
          </li>
        ))}
      </ul>
      <form data-testid="project-tag-add-form" onSubmit={onAdd}>
        <select
          data-testid="project-tag-add"
          value={selected}
          onChange={(e) => setSelected(e.target.value)}
        >
          {catalogue.map((t) => (
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
