import { useEffect, useState, type FormEvent } from 'react';
import { formatMoney } from '../../ui/money';
import { InvoiceLineItems } from './InvoiceLineItems';
import { ProjectTags } from './ProjectTags';
import { ProjectNotes } from './ProjectNotes';

// Opened from the projects list: a project's detail view lists ITS invoices, shows the derived
// total, and lets you add a new invoice for an amount. Invoices are scoped to the project via
// /api/projects/<id>/invoices. Money renders with exactly two decimals.
type Invoice = {
  id: number;
  projectId: number;
  amount: number;
  status: string;
  issuedDate: string;
  dueDate: string;
  // How much is still left to pay: the invoice amount minus any payments recorded against it.
  dueAmount: number;
};

type Task = {
  id: number;
  projectId: number;
  title: string;
  done: boolean;
};

// A project's budget position: the budget set on it, how much has been invoiced against it, and what
// is left (budget minus invoiced).
type Budget = {
  budget: number;
  invoiced: number;
  remaining: number;
};

export function ProjectDetail({ projectId }: { projectId: number }) {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [budget, setBudget] = useState<Budget | null>(null);
  const [tasks, setTasks] = useState<Task[]>([]);
  // Which tasks to show: all of them, just the open ones, or just the finished ones.
  const [taskFilter, setTaskFilter] = useState('');
  const [amount, setAmount] = useState('');
  const [amountError, setAmountError] = useState(false);
  const [openInvoiceId, setOpenInvoiceId] = useState<number | null>(null);

  async function load() {
    const res = await fetch(`/api/projects/${projectId}/invoices`);
    if (res.ok) {
      setInvoices(await res.json());
    }
    const budgetRes = await fetch(`/api/projects/${projectId}/budget`);
    if (budgetRes.ok) {
      setBudget(await budgetRes.json());
    }
  }

  async function loadTasks() {
    const query = taskFilter ? `?filter=${encodeURIComponent(taskFilter)}` : '';
    const res = await fetch(`/api/projects/${projectId}/tasks${query}`);
    if (res.ok) {
      setTasks(await res.json());
    }
  }

  useEffect(() => {
    void load();
  }, [projectId]);

  useEffect(() => {
    void loadTasks();
  }, [projectId, taskFilter]);

  // Tick a task off (or back on): toggles OPEN <-> DONE and reloads the list.
  async function onToggleTask(taskId: number) {
    const res = await fetch(`/api/tasks/${taskId}/toggle`, { method: 'POST' });
    if (res.ok) {
      await loadTasks();
    }
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    // An invoice must be for a real amount: a positive number, never zero or negative.
    const value = Number(amount);
    if (!(value > 0)) {
      setAmountError(true);
      return;
    }
    setAmountError(false);
    const res = await fetch(`/api/projects/${projectId}/invoices`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ amount: Number(amount) }),
    });
    if (res.ok) {
      setAmount('');
      await load();
    }
  }

  // Sort this project's invoices by due date (earliest due first) via the by-due endpoint.
  async function onSortByDue() {
    const res = await fetch(`/api/projects/${projectId}/invoices/by-due`);
    if (res.ok) {
      setInvoices(await res.json());
    }
  }

  async function onSend(invoiceId: number) {
    const res = await fetch(`/api/invoices/${invoiceId}/send`, { method: 'POST' });
    if (res.ok) {
      await load();
    }
  }

  // Cancel (void) an invoice sent by mistake: it reads VOID and stops counting toward what is owed.
  async function onCancel(invoiceId: number) {
    const res = await fetch(`/api/invoices/${invoiceId}/cancel`, { method: 'POST' });
    if (res.ok) {
      await load();
    }
  }

  const total = invoices.reduce((sum, invoice) => sum + Number(invoice.amount), 0);

  // Opening an invoice drills into its own detail view (line items, payments, notes); it takes
  // over the project view so the project's own panels don't double up with the invoice's.
  if (openInvoiceId !== null) {
    return (
      <section data-testid="project-detail">
        <InvoiceLineItems
          invoiceId={openInvoiceId}
          onBack={() => {
            setOpenInvoiceId(null);
            void load();
          }}
        />
      </section>
    );
  }

  return (
    <section data-testid="project-detail">
      {budget && (
        <dl data-testid="project-budget-summary">
          <dt>Budget</dt>
          <dd data-testid="project-budget">{formatMoney(budget.budget)}</dd>
          <dt>Invoiced</dt>
          <dd data-testid="project-invoiced">{formatMoney(budget.invoiced)}</dd>
          <dt>Remaining</dt>
          <dd data-testid="project-remaining">{formatMoney(budget.remaining)}</dd>
        </dl>
      )}

      <ProjectTags projectId={projectId} />

      <ProjectNotes projectId={projectId} />

      <h2>Tasks</h2>

      <label>
        Show
        <select
          data-testid="task-filter"
          value={taskFilter}
          onChange={(e) => setTaskFilter(e.target.value)}
        >
          <option value="">All tasks</option>
          <option value="OPEN">Open</option>
          <option value="DONE">Done</option>
        </select>
      </label>

      <table data-testid="project-tasks-table">
        <thead>
          <tr>
            <th>Task</th>
            <th>Status</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {tasks.map((task) => (
            <tr key={task.id} data-testid={`task-row-${task.id}`}>
              <td data-testid="task-title">{task.title}</td>
              <td data-testid="task-status">{task.done ? 'DONE' : 'OPEN'}</td>
              <td>
                <button
                  data-testid={`task-toggle-${task.id}`}
                  type="button"
                  onClick={() => onToggleTask(task.id)}
                >
                  {task.done ? 'Reopen' : 'Tick off'}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <h2>Invoices</h2>

      <form data-testid="invoice-form" onSubmit={onSubmit}>
        <input
          data-testid="invoice-form-amount"
          placeholder="Amount"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
        />
        {amountError && (
          <p data-testid="invoice-form-amount-error">
            Enter an amount greater than zero.
          </p>
        )}
        <button data-testid="invoice-form-submit" type="submit">
          Add invoice
        </button>
      </form>

      <button data-testid="invoice-sort-due" type="button" onClick={onSortByDue}>
        Sort by due date
      </button>

      <table data-testid="project-invoices-table">
        <thead>
          <tr>
            <th>Amount</th>
            <th>Left to pay</th>
            <th>Issued</th>
            <th>Due</th>
            <th>Status</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {invoices.map((invoice) => (
            <tr key={invoice.id} data-testid={`invoice-row-${invoice.id}`}>
              <td data-testid="invoice-amount">{formatMoney(invoice.amount)}</td>
              <td data-testid="invoice-due-amount">{formatMoney(invoice.dueAmount)}</td>
              <td data-testid="invoice-issued">{invoice.issuedDate}</td>
              <td data-testid="invoice-due">{invoice.dueDate}</td>
              <td data-testid="invoice-status">{invoice.status}</td>
              <td>
                <button
                  data-testid={`invoice-open-${invoice.id}`}
                  type="button"
                  onClick={() => setOpenInvoiceId(invoice.id)}
                >
                  Open
                </button>
                {/* Lifecycle: a DRAFT can be sent. Once sent, the status is worked out from the
                    payments recorded against the invoice — PARTIAL then PAID — not flipped by hand. */}
                {invoice.status === 'DRAFT' && (
                  <button
                    data-testid={`invoice-send-${invoice.id}`}
                    type="button"
                    onClick={() => onSend(invoice.id)}
                  >
                    Send
                  </button>
                )}
                {/* A sent invoice can be cancelled (voided) if it went out by mistake. */}
                {invoice.status === 'SENT' && (
                  <button
                    data-testid={`invoice-cancel-${invoice.id}`}
                    type="button"
                    onClick={() => onCancel(invoice.id)}
                  >
                    Cancel
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr>
            <td data-testid="project-invoices-total">{formatMoney(total)}</td>
          </tr>
        </tfoot>
      </table>
    </section>
  );
}
