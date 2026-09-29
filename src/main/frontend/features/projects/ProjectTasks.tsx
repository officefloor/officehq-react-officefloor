import React, { useEffect, useState } from 'react';

// A project's task checklist, shown when a project is opened from the projects list. Owns its own
// state (CLAUDE.md — features own their state). Each task can be ticked off (OPEN <-> DONE).
type Task = { id: number; projectId: number; title: string; done: boolean };
type Filter = 'ALL' | 'OPEN' | 'DONE';

export function ProjectTasks({ projectId }: { projectId: number }) {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [filter, setFilter] = useState<Filter>('ALL');

  async function load() {
    const query = filter === 'ALL' ? '' : `&status=${filter}`;
    const res = await fetch(`/api/tasks?projectId=${projectId}${query}`);
    if (res.ok) {
      setTasks(await res.json());
    }
  }

  useEffect(() => {
    void load();
  }, [projectId, filter]);

  async function toggle(id: number) {
    const res = await fetch('/api/tasks/toggle', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id }),
    });
    if (res.ok) {
      // Reload so the ticked-off task drops out of (or into) the active filter.
      await load();
    }
  }

  return (
    <section data-testid="project-tasks">
      <h2>Tasks</h2>
      <label>
        Show
        <select
          data-testid="task-filter"
          value={filter}
          onChange={(e) => setFilter(e.target.value as Filter)}
        >
          <option value="ALL">All</option>
          <option value="OPEN">Open</option>
          <option value="DONE">Done</option>
        </select>
      </label>
      <table data-testid="project-tasks-table">
        <thead>
          <tr>
            <th>Task</th>
            <th>Status</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {tasks.map((t) => (
            <tr key={t.id} data-testid={`task-row-${t.id}`}>
              <td data-testid="task-title">{t.title}</td>
              <td data-testid="task-status">{t.done ? 'DONE' : 'OPEN'}</td>
              <td>
                <button
                  type="button"
                  data-testid={`task-toggle-${t.id}`}
                  onClick={() => void toggle(t.id)}
                >
                  {t.done ? 'Reopen' : 'Tick off'}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
