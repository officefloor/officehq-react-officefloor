import React, { useEffect, useState } from 'react';

// A project's task checklist, shown when a project is opened from the projects list. Owns its own
// state (CLAUDE.md — features own their state). Each task can be ticked off (OPEN <-> DONE).
type Task = { id: number; projectId: number; title: string; done: boolean };

export function ProjectTasks({ projectId }: { projectId: number }) {
  const [tasks, setTasks] = useState<Task[]>([]);

  async function load() {
    const res = await fetch(`/api/tasks?projectId=${projectId}`);
    if (res.ok) {
      setTasks(await res.json());
    }
  }

  useEffect(() => {
    void load();
  }, [projectId]);

  async function toggle(id: number) {
    const res = await fetch('/api/tasks/toggle', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id }),
    });
    if (res.ok) {
      const updated: Task = await res.json();
      setTasks((prev) => prev.map((t) => (t.id === updated.id ? updated : t)));
    }
  }

  return (
    <section data-testid="project-tasks">
      <h2>Tasks</h2>
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
