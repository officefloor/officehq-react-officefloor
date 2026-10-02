import { useEffect, useState, type FormEvent } from 'react';

// A project can be labelled with tags (chips) to group it. This panel shows the project's current
// tags, lets you add one from the tags that aren't on it yet, and remove any of them. Tags live in
// the shared tags catalogue; assignments are scoped to the project via /api/projects/<id>/tags.
type Tag = { id: number; name: string };

export function ProjectTags({ projectId }: { projectId: number }) {
  const [tags, setTags] = useState<Tag[]>([]);
  const [allTags, setAllTags] = useState<Tag[]>([]);
  const [tagId, setTagId] = useState('');

  async function loadTags() {
    const res = await fetch(`/api/projects/${projectId}/tags`);
    if (res.ok) {
      setTags(await res.json());
    }
  }

  async function loadAllTags() {
    const res = await fetch('/api/tags');
    if (res.ok) {
      setAllTags(await res.json());
    }
  }

  useEffect(() => {
    void loadTags();
    void loadAllTags();
  }, [projectId]);

  // Only offer tags that aren't already on the project.
  const assigned = new Set(tags.map((tag) => tag.id));
  const available = allTags.filter((tag) => !assigned.has(tag.id));

  async function onAdd(event: FormEvent) {
    event.preventDefault();
    if (!tagId) {
      return;
    }
    const res = await fetch(`/api/projects/${projectId}/tags`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ tagId: Number(tagId) }),
    });
    if (res.ok) {
      setTagId('');
      setTags(await res.json());
    }
  }

  async function onRemove(id: number) {
    const res = await fetch(`/api/projects/${projectId}/tags/${id}/remove`, { method: 'POST' });
    if (res.ok) {
      setTags(await res.json());
    }
  }

  return (
    <section data-testid="project-tags">
      <h2>Tags</h2>

      <ul data-testid="project-tags-list">
        {tags.map((tag) => (
          <li key={tag.id}>
            <span data-testid={`project-tag-${tag.id}`}>{tag.name}</span>
            <button
              data-testid={`project-tag-remove-${tag.id}`}
              type="button"
              onClick={() => void onRemove(tag.id)}
            >
              Remove
            </button>
          </li>
        ))}
      </ul>

      <form data-testid="project-tag-form" onSubmit={onAdd}>
        <select
          data-testid="project-tag-add"
          value={tagId}
          onChange={(e) => setTagId(e.target.value)}
        >
          <option value="">Select a tag</option>
          {available.map((tag) => (
            <option key={tag.id} value={tag.id}>
              {tag.name}
            </option>
          ))}
        </select>
        <button data-testid="project-tag-add-submit" type="submit">
          Add tag
        </button>
      </form>
    </section>
  );
}
