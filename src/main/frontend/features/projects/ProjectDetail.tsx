import { useEffect, useState, type FormEvent } from 'react';
import { formatMoney } from '../../ui/money';
import { InvoiceLineItems } from './InvoiceLineItems';

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
};

type Task = {
  id: number;
  projectId: number;
  title: string;
  done: boolean;
};

export function ProjectDetail({ projectId }: { projectId: number }) {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
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

  async function onPay(invoiceId: number) {
    const res = await fetch(`/api/invoices/${invoiceId}/pay`, { method: 'POST' });
    if (res.ok) {
      await load();
    }
  }

  const total = invoices.reduce((sum, invoice) => sum + Number(invoice.amount), 0);

  return (
    <section data-testid="project-detail">
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

      {openInvoiceId !== null ? (
        <InvoiceLineItems
          invoiceId={openInvoiceId}
          onBack={() => {
            setOpenInvoiceId(null);
            void load();
          }}
        />
      ) : (
      <>
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
                {/* Lifecycle: a DRAFT can be sent; only a SENT invoice can be paid. */}
                {invoice.status === 'DRAFT' && (
                  <button
                    data-testid={`invoice-send-${invoice.id}`}
                    type="button"
                    onClick={() => onSend(invoice.id)}
                  >
                    Send
                  </button>
                )}
                {invoice.status === 'SENT' && (
                  <button
                    data-testid={`invoice-pay-${invoice.id}`}
                    type="button"
                    onClick={() => onPay(invoice.id)}
                  >
                    Mark paid
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
      </>
      )}
    </section>
  );
}
