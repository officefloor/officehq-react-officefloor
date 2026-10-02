import React, { useEffect, useState } from 'react';

// A project's task checklist, rendered inside the projects feature when a project is opened. Lists
// the project's tasks and lets the owner tick each one off (OPEN <-> DONE). Owns its own state and
// data loading (no global store); composed, not branched.
type Task = { id: number; projectId: number; title: string; done: boolean };

export function ProjectTasks({ projectId }: { projectId: number }) {
  const [tasks, setTasks] = useState<Task[]>([]);

  async function load() {
    const res = await fetch(`/api/projects/${projectId}/tasks`);
    setTasks(await res.json());
  }

  useEffect(() => {
    void load();
  }, [projectId]);

  async function toggle(taskId: number) {
    const res = await fetch(`/api/tasks/${taskId}/toggle`, { method: 'POST' });
    if (!res.ok) {
      return;
    }
    await load();
  }

  return (
    <section data-testid="project-tasks">
      <h2>Tasks</h2>

      <table data-testid="project-tasks-table">
        <thead>
          <tr>
            <th>Task</th>
            <th>Status</th>
            <th></th>
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
