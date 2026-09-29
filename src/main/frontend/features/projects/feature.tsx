import React, { useEffect, useState } from 'react';
import type { Feature } from '../../router/routes';

// Projects feature: add a project and pick which client it is for, then list every project showing
// the client's NAME. Opening a project (project-open-<id>) reveals its detail: the invoices raised
// against it, their running total, and a form to add another. Owns its own state; talks to its own
// /api/projects and /api/invoices endpoints and reads /api/clients to populate the client picker.
// data-testid anchors follow the spec's conventions.
type Client = { id: number; name: string; email: string };
type Project = { id: number; name: string; clientId: number; clientName: string };
type Invoice = {
  id: number;
  projectId: number;
  amount: number;
  status: string;
  issuedDate: string;
  dueDate: string;
};
type Task = { id: number; projectId: number; title: string; done: boolean };

function money(amount: number): string {
  return `$${Number(amount).toFixed(2)}`;
}

// A project's detail: its invoices, what they add up to, and a form to add a new invoice. Owns its
// own invoice state, scoped to the one project it is showing.
function ProjectDetail({ project }: { project: Project }) {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [amount, setAmount] = useState('');
  const [amountError, setAmountError] = useState('');
  const [sortByDue, setSortByDue] = useState(false);

  async function load() {
    const res = await fetch(`/api/invoices?projectId=${project.id}`);
    setInvoices(await res.json());
  }

  async function loadTasks() {
    const res = await fetch(`/api/tasks?projectId=${project.id}`);
    setTasks(await res.json());
  }

  useEffect(() => {
    void load();
    void loadTasks();
  }, [project.id]);

  async function toggleTask(id: number) {
    await fetch(`/api/tasks/${id}/toggle`, { method: 'POST' });
    await loadTasks();
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    const value = Number(amount);
    if (amount.trim() === '' || !Number.isFinite(value) || value <= 0) {
      setAmountError('Amount must be more than zero');
      return;
    }
    setAmountError('');
    await fetch('/api/invoices', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ projectId: project.id, amount: Number(amount) }),
    });
    setAmount('');
    await load();
  }

  async function send(id: number) {
    await fetch(`/api/invoices/${id}/send`, { method: 'POST' });
    await load();
  }

  async function pay(id: number) {
    await fetch(`/api/invoices/${id}/pay`, { method: 'POST' });
    await load();
  }

  const total = invoices.reduce((sum, inv) => sum + Number(inv.amount), 0);
  const shownInvoices = sortByDue
    ? [...invoices].sort((a, b) => a.dueDate.localeCompare(b.dueDate))
    : invoices;

  return (
    <section data-testid="project-detail">
      <h2 data-testid="project-detail-name">{project.name}</h2>

      <table data-testid="project-tasks-table">
        <tbody>
          {tasks.map((t) => (
            <tr key={t.id} data-testid={`task-row-${t.id}`}>
              <td data-testid="task-title">{t.title}</td>
              <td data-testid="task-status">{t.done ? 'DONE' : 'OPEN'}</td>
              <td>
                <button data-testid={`task-toggle-${t.id}`} onClick={() => toggleTask(t.id)}>
                  {t.done ? 'Reopen' : 'Tick off'}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <form data-testid="invoice-form" onSubmit={submit}>
        <input
          data-testid="invoice-form-amount"
          placeholder="Amount"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
        />
        <button data-testid="invoice-form-submit" type="submit">
          Add invoice
        </button>
        {amountError && (
          <p data-testid="invoice-form-amount-error" role="alert">
            {amountError}
          </p>
        )}
      </form>

      <button data-testid="invoice-sort-due" type="button" onClick={() => setSortByDue(true)}>
        Sort by due date
      </button>

      <table data-testid="project-invoices-table">
        <tbody>
          {shownInvoices.map((inv) => (
            <tr key={inv.id} data-testid={`invoice-row-${inv.id}`}>
              <td data-testid="invoice-amount">{money(inv.amount)}</td>
              <td data-testid="invoice-issued">{inv.issuedDate}</td>
              <td data-testid="invoice-due">{inv.dueDate}</td>
              <td data-testid="invoice-status">{inv.status}</td>
              <td>
                {inv.status === 'DRAFT' && (
                  <button data-testid={`invoice-send-${inv.id}`} onClick={() => send(inv.id)}>
                    Send
                  </button>
                )}
                {inv.status === 'SENT' && (
                  <button data-testid={`invoice-pay-${inv.id}`} onClick={() => pay(inv.id)}>
                    Mark paid
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <p data-testid="project-invoices-total">{money(total)}</p>
    </section>
  );
}

function ProjectsPage() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [clients, setClients] = useState<Client[]>([]);
  const [name, setName] = useState('');
  const [clientId, setClientId] = useState('');
  const [openId, setOpenId] = useState<number | null>(null);

  async function loadProjects() {
    const res = await fetch('/api/projects');
    setProjects(await res.json());
  }

  async function loadClients() {
    const res = await fetch('/api/clients');
    setClients(await res.json());
  }

  useEffect(() => {
    void loadProjects();
    void loadClients();
  }, []);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!name.trim() || !clientId) {
      return;
    }
    await fetch('/api/projects', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, clientId: Number(clientId) }),
    });
    setName('');
    setClientId('');
    await loadProjects();
  }

  const open = projects.find((p) => p.id === openId) ?? null;

  return (
    <section data-testid="projects">
      <form data-testid="project-form" onSubmit={submit}>
        <input
          data-testid="project-form-name"
          placeholder="Project name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <select
          data-testid="project-form-client"
          value={clientId}
          onChange={(e) => setClientId(e.target.value)}
        >
          <option value="">Select a client</option>
          {clients.map((c) => (
            <option key={c.id} value={String(c.id)}>
              {c.name}
            </option>
          ))}
        </select>
        <button data-testid="project-form-submit" type="submit">
          Add project
        </button>
      </form>

      {projects.length === 0 ? (
        <p data-testid="projects-empty">No projects yet.</p>
      ) : (
        <table data-testid="projects-table">
          <tbody>
            {projects.map((p) => (
              <tr key={p.id} data-testid={`project-row-${p.id}`}>
                <td data-testid="project-name">{p.name}</td>
                <td data-testid="project-client">{p.clientName}</td>
                <td>
                  <button data-testid={`project-open-${p.id}`} onClick={() => setOpenId(p.id)}>
                    Open
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {open && <ProjectDetail key={open.id} project={open} />}
    </section>
  );
}

export const feature: Feature = { id: 'projects', label: 'Projects', Page: ProjectsPage };
