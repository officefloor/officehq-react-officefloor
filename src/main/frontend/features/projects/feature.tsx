import React, { useEffect, useState } from 'react';
import type { Feature } from '../../router/routes';
import { useJsonResource } from '../../ui/useJsonResource';

// Projects feature: add a project and pick which client it is for, then list every project showing
// the client's NAME. Opening a project (project-open-<id>) reveals its detail: the invoices raised
// against it, their running total, and a form to add another. Owns its own state; talks to its own
// /api/projects and /api/invoices endpoints and reads /api/clients to populate the client picker.
// data-testid anchors follow the spec's conventions.
type Client = { id: number; name: string; email: string };
type ProjectStatus = 'ACTIVE' | 'ON_HOLD' | 'FINISHED';
type Project = {
  id: number;
  name: string;
  code: string;
  clientId: number;
  clientName: string;
  archived: boolean;
  status: ProjectStatus;
};
type Invoice = {
  id: number;
  projectId: number;
  amount: number;
  amountDue: number;
  status: string;
  issuedDate: string;
  dueDate: string;
  discountPct: number;
  taxPct: number;
  // The currency the invoice's client is paid in; the invoice's money is shown in it.
  currency: string;
};
type Task = { id: number; projectId: number; title: string; done: boolean };
type Tag = { id: number; name: string };
type Note = { id: number; targetType: string; targetId: number; text: string; at: string };

type LineItem = {
  id: number;
  invoiceId: number;
  description: string;
  qty: number;
  unit: string;
  unitPrice: number;
};

type Payment = { id: number; invoiceId: number; amount: number; date: string };

// Each client is paid in their own currency; an invoice's money is shown with that currency's symbol.
const CURRENCY_SYMBOLS: Record<string, string> = { USD: '$', EUR: '€', GBP: '£' };

function money(amount: number, currency: string = 'USD'): string {
  const symbol = CURRENCY_SYMBOLS[currency] ?? '$';
  return `${symbol}${Number(amount).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
}

// Budget figures are shown with thousands separators (e.g. $1,000.00) so larger amounts stay
// readable at a glance.
function moneyGrouped(amount: number): string {
  return `$${Number(amount).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
}

type ProjectBudget = { budget: number | null; invoiced: number; remaining: number | null };

// One charge line while it is being edited on screen: the fields are held as raw strings so the
// inputs stay controlled and the total can recompute live as they change.
type EditItem = { id: number; description: string; qty: string; unit: string; unitPrice: string };

// An opened invoice's detail: the things it is charging for, listed as line items (description, how
// many, price each), the total worked out for you, and a form to add another line. Each line can be
// changed in place (save) or removed, and the total re-derives from whatever lines remain. Owns its
// own line-item state, scoped to the one invoice it is showing.
function InvoiceDetail({
  invoiceId,
  onChange,
  onClose,
}: {
  invoiceId: number;
  onChange: () => void;
  onClose: () => void;
}) {
  const [items, setItems] = useState<EditItem[]>([]);
  const [description, setDescription] = useState('');
  const [qty, setQty] = useState('');
  const [unit, setUnit] = useState('');
  const [unitPrice, setUnitPrice] = useState('');
  const [payments, setPayments] = useState<Payment[]>([]);
  const [paymentAmount, setPaymentAmount] = useState('');
  const [paymentDate, setPaymentDate] = useState('');
  const [status, setStatus] = useState('');
  // The percentage discount taken off this invoice's subtotal (0 unless one is set).
  const [discountPct, setDiscountPct] = useState(0);
  // The percentage sales tax added on top after the discount (0 unless one is set).
  const [taxPct, setTaxPct] = useState(0);
  // The currency the invoice's client is paid in; its money is shown in it. Read alongside the
  // status from the invoice itself.
  const [currency, setCurrency] = useState('USD');

  // The status is worked out from the payments recorded against the invoice, so re-read it whenever
  // a payment is added rather than flipping it by hand. The discount and tax percentages come back
  // on the same read.
  async function loadStatus() {
    const res = await fetch(`/api/invoices/${invoiceId}`);
    const inv: Invoice = await res.json();
    setStatus(inv.status);
    setDiscountPct(Number(inv.discountPct) || 0);
    setTaxPct(Number(inv.taxPct) || 0);
    setCurrency(inv.currency ?? 'USD');
  }

  async function load() {
    const res = await fetch(`/api/invoices/${invoiceId}/line-items`);
    const rows: LineItem[] = await res.json();
    setItems(
      rows.map((li) => ({
        id: li.id,
        description: li.description,
        qty: String(li.qty),
        unit: li.unit,
        unitPrice: String(li.unitPrice),
      })),
    );
  }

  async function loadPayments() {
    const res = await fetch(`/api/invoices/${invoiceId}/payments`);
    setPayments(await res.json());
  }

  async function submitPayment(e: React.FormEvent) {
    e.preventDefault();
    const value = Number(paymentAmount);
    if (paymentAmount.trim() === '' || !Number.isFinite(value) || value <= 0) {
      return;
    }
    if (paymentDate.trim() === '') {
      return;
    }
    await fetch(`/api/invoices/${invoiceId}/payments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ amount: value, date: paymentDate }),
    });
    setPaymentAmount('');
    setPaymentDate('');
    await loadPayments();
    await loadStatus();
    onChange();
  }

  useEffect(() => {
    void load();
    void loadPayments();
    void loadStatus();
  }, [invoiceId]);

  function editField(
    id: number,
    field: 'description' | 'qty' | 'unit' | 'unitPrice',
    value: string,
  ) {
    setItems((prev) => prev.map((li) => (li.id === id ? { ...li, [field]: value } : li)));
  }

  function amountOf(li: EditItem): number {
    const line = Number(li.qty) * Number(li.unitPrice);
    return Number.isFinite(line) ? line : 0;
  }

  async function save(li: EditItem) {
    if (!li.description.trim() || li.qty.trim() === '' || li.unitPrice.trim() === '') {
      return;
    }
    await fetch(`/api/invoices/${invoiceId}/line-items/${li.id}/update`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        description: li.description,
        qty: Number(li.qty),
        unit: li.unit,
        unitPrice: Number(li.unitPrice),
      }),
    });
    await load();
    onChange();
  }

  async function remove(id: number) {
    await fetch(`/api/invoices/${invoiceId}/line-items/${id}/remove`, { method: 'POST' });
    await load();
    onChange();
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!description.trim() || qty.trim() === '' || unitPrice.trim() === '') {
      return;
    }
    await fetch(`/api/invoices/${invoiceId}/line-items`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        description,
        qty: Number(qty),
        unit,
        unitPrice: Number(unitPrice),
      }),
    });
    setDescription('');
    setQty('');
    setUnit('');
    setUnitPrice('');
    await load();
    onChange();
  }

  // The subtotal is the sum of the line items; the discount is that percentage taken off it. Sales
  // tax is then added on top of the discounted base (worked out after the discount), and the final
  // total is the subtotal less the discount plus that tax.
  const subtotal = items.reduce((sum, li) => sum + amountOf(li), 0);
  const discount = (subtotal * discountPct) / 100;
  const tax = ((subtotal - discount) * taxPct) / 100;
  const total = subtotal - discount + tax;

  return (
    <section data-testid="invoice-detail">
      <button data-testid="invoice-detail-close" type="button" onClick={onClose}>
        Back to invoices
      </button>

      <p data-testid="invoice-status">{status}</p>

      <table data-testid="invoice-lineitems-table">
        <tbody>
          {items.map((li) => (
            <tr key={li.id} data-testid={`lineitem-row-${li.id}`}>
              <td>
                <input
                  data-testid="lineitem-description"
                  value={li.description}
                  onChange={(e) => editField(li.id, 'description', e.target.value)}
                />
              </td>
              <td data-testid="lineitem-qty">{li.qty}</td>
              <td data-testid="lineitem-unit">{li.unit}</td>
              <td>
                <input
                  data-testid="lineitem-unitprice"
                  value={li.unitPrice}
                  onChange={(e) => editField(li.id, 'unitPrice', e.target.value)}
                />
              </td>
              <td data-testid="lineitem-amount">{money(amountOf(li), currency)}</td>
              <td>
                <button
                  data-testid={`lineitem-save-${li.id}`}
                  type="button"
                  onClick={() => save(li)}
                >
                  Save
                </button>
                <button
                  data-testid={`lineitem-remove-${li.id}`}
                  type="button"
                  onClick={() => remove(li.id)}
                >
                  Remove
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <p data-testid="invoice-subtotal">{money(subtotal, currency)}</p>
      <p data-testid="invoice-discount">{money(discount, currency)}</p>
      <p data-testid="invoice-tax">{money(tax, currency)}</p>
      <p data-testid="invoice-amount">{money(total, currency)}</p>

      <form data-testid="lineitem-form" onSubmit={submit}>
        <input
          data-testid="lineitem-form-description"
          placeholder="Description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
        <input
          data-testid="lineitem-form-qty"
          placeholder="How many"
          value={qty}
          onChange={(e) => setQty(e.target.value)}
        />
        <input
          data-testid="lineitem-form-unit"
          placeholder="Unit"
          value={unit}
          onChange={(e) => setUnit(e.target.value)}
        />
        <input
          data-testid="lineitem-form-unitprice"
          placeholder="Price each"
          value={unitPrice}
          onChange={(e) => setUnitPrice(e.target.value)}
        />
        <button data-testid="lineitem-form-submit" type="submit">
          Add line item
        </button>
      </form>

      <table data-testid="invoice-payments-table">
        <tbody>
          {payments.map((p) => (
            <tr key={p.id} data-testid={`payment-row-${p.id}`}>
              <td data-testid="payment-amount">{money(p.amount, currency)}</td>
              <td data-testid="payment-date">{p.date}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <form data-testid="payment-form" onSubmit={submitPayment}>
        <input
          data-testid="payment-form-amount"
          placeholder="Amount paid"
          value={paymentAmount}
          onChange={(e) => setPaymentAmount(e.target.value)}
        />
        <input
          data-testid="payment-form-date"
          type="date"
          value={paymentDate}
          onChange={(e) => setPaymentDate(e.target.value)}
        />
        <button data-testid="payment-form-submit" type="submit">
          Record payment
        </button>
      </form>

      <NotesPanel targetType="invoice" targetId={invoiceId} wrapperTestId="invoice-notes" />
    </section>
  );
}

// Which lifecycle actions an invoice offers depends on where it is in its lifecycle: a draft can be
// sent, a sent invoice can be marked paid. Collecting the mapping here keeps the row markup flat and
// gives one obvious place to add a new lifecycle action.
function InvoiceActions({
  invoice,
  onOpen,
  onSend,
  onPay,
  onCancel,
}: {
  invoice: Invoice;
  onOpen: () => void;
  onSend: () => void;
  onPay: () => void;
  onCancel: () => void;
}) {
  return (
    <>
      <button data-testid={`invoice-open-${invoice.id}`} onClick={onOpen}>
        Open
      </button>
      {invoice.status === 'DRAFT' && (
        <button data-testid={`invoice-send-${invoice.id}`} onClick={onSend}>
          Send
        </button>
      )}
      {invoice.status === 'SENT' && (
        <button data-testid={`invoice-pay-${invoice.id}`} onClick={onPay}>
          Mark paid
        </button>
      )}
      {invoice.status === 'SENT' && (
        <button data-testid={`invoice-cancel-${invoice.id}`} onClick={onCancel}>
          Cancel
        </button>
      )}
    </>
  );
}

// The invoices raised against a project: what they add up to, a form to raise another, and a drill-in
// to each invoice's line items and payments. Owns its own invoice state, scoped to the one project.
// Whenever the invoices change (a new one raised, a status moved, a line item edited) the owner is
// told so figures derived from them — the budget's invoiced/remaining — can be refreshed.
function ProjectInvoices({
  projectId,
  onInvoicesChanged,
  openInvoiceId,
  setOpenInvoiceId,
}: {
  projectId: number;
  onInvoicesChanged: () => void;
  openInvoiceId: number | null;
  setOpenInvoiceId: (id: number | null) => void;
}) {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [amount, setAmount] = useState('');
  const [amountError, setAmountError] = useState('');
  const [sortByDue, setSortByDue] = useState(false);

  async function load() {
    const res = await fetch(`/api/invoices?projectId=${projectId}`);
    setInvoices(await res.json());
    onInvoicesChanged();
  }

  useEffect(() => {
    void load();
  }, [projectId]);

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
      body: JSON.stringify({ projectId, amount: value }),
    });
    setAmount('');
    await load();
  }

  // A lifecycle move (send, mark paid, …) is a POST to the matching action route followed by a reload;
  // routing them through one place means a new action is one more call site, not new plumbing.
  async function transition(id: number, action: 'send' | 'pay' | 'cancel') {
    await fetch(`/api/invoices/${id}/${action}`, { method: 'POST' });
    await load();
  }

  const total = invoices.reduce((sum, inv) => sum + Number(inv.amount), 0);
  // Every invoice on a project belongs to the same client, so they share one currency; show the
  // running total in it (defaulting to USD before any invoice has loaded).
  const currency = invoices[0]?.currency ?? 'USD';
  const shownInvoices = sortByDue
    ? [...invoices].sort((a, b) => a.dueDate.localeCompare(b.dueDate))
    : invoices;

  return (
    <>
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

      {openInvoiceId === null ? (
        <>
          <table data-testid="project-invoices-table">
            <tbody>
              {shownInvoices.map((inv) => (
                <tr key={inv.id} data-testid={`invoice-row-${inv.id}`}>
                  <td data-testid="invoice-amount">{money(inv.amount, inv.currency)}</td>
                  <td data-testid="invoice-due-amount">
                    {money(inv.amountDue, inv.currency)}
                  </td>
                  <td data-testid="invoice-issued">{inv.issuedDate}</td>
                  <td data-testid="invoice-due">{inv.dueDate}</td>
                  <td data-testid="invoice-status">{inv.status}</td>
                  <td>
                    <InvoiceActions
                      invoice={inv}
                      onOpen={() => setOpenInvoiceId(inv.id)}
                      onSend={() => transition(inv.id, 'send')}
                      onPay={() => transition(inv.id, 'pay')}
                      onCancel={() => transition(inv.id, 'cancel')}
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <p data-testid="project-invoices-total">{money(total, currency)}</p>
        </>
      ) : (
        <InvoiceDetail
          invoiceId={openInvoiceId}
          onChange={load}
          onClose={() => setOpenInvoiceId(null)}
        />
      )}
    </>
  );
}

// A project's budget: what was set for it, what has been invoiced against it so far, and what is
// left over. Invoiced (and so remaining) is derived from the project's invoices, so the owner bumps
// `reloadKey` whenever they change and this re-reads the budget picture. Owns its own budget state.
function BudgetPanel({ projectId, reloadKey }: { projectId: number; reloadKey: number }) {
  const [budget, setBudget] = useState<ProjectBudget | null>(null);
  const [budgetInput, setBudgetInput] = useState('');

  async function load() {
    const res = await fetch(`/api/projects/${projectId}/budget`);
    setBudget(await res.json());
  }

  useEffect(() => {
    void load();
  }, [projectId, reloadKey]);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    const value = Number(budgetInput);
    if (budgetInput.trim() === '' || !Number.isFinite(value) || value < 0) {
      return;
    }
    await fetch(`/api/projects/${projectId}/budget`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ budget: value }),
    });
    setBudgetInput('');
    await load();
  }

  return (
    <section data-testid="project-budget-panel">
      <p data-testid="project-budget">
        {budget && budget.budget != null ? moneyGrouped(budget.budget) : ''}
      </p>
      <p data-testid="project-invoiced">{moneyGrouped(budget ? budget.invoiced : 0)}</p>
      <p data-testid="project-remaining">
        {budget && budget.remaining != null ? moneyGrouped(budget.remaining) : ''}
      </p>
      <form data-testid="project-budget-form" onSubmit={submit}>
        <input
          data-testid="project-budget-input"
          placeholder="Budget"
          value={budgetInput}
          onChange={(e) => setBudgetInput(e.target.value)}
        />
        <button data-testid="project-budget-submit" type="submit">
          Set budget
        </button>
      </form>
    </section>
  );
}

// A project's labels: the ones on it now (each removable) and a picker of the remaining labels to add
// another. Owns its own tag state, scoped to the one project.
function TagsPanel({ projectId }: { projectId: number }) {
  const [tags, reloadTags] = useJsonResource<Tag[]>(`/api/projects/${projectId}/tags`, []);
  const [allTags, reloadAllTags] = useJsonResource<Tag[]>('/api/tags', []);
  const [tagToAdd, setTagToAdd] = useState('');

  async function reload() {
    await Promise.all([reloadTags(), reloadAllTags()]);
  }

  async function addTag(e: React.FormEvent) {
    e.preventDefault();
    if (!tagToAdd) {
      return;
    }
    await fetch(`/api/projects/${projectId}/tags`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ tagId: Number(tagToAdd) }),
    });
    setTagToAdd('');
    await reload();
  }

  async function removeTag(id: number) {
    await fetch(`/api/projects/${projectId}/tags/${id}/remove`, { method: 'POST' });
    await reload();
  }

  return (
    <>
      <div data-testid="project-tags">
        {tags.map((tag) => (
          <span key={tag.id}>
            <span data-testid={`project-tag-${tag.id}`}>{tag.name}</span>
            <button
              data-testid={`project-tag-remove-${tag.id}`}
              type="button"
              onClick={() => removeTag(tag.id)}
            >
              Remove
            </button>
          </span>
        ))}
      </div>

      <form data-testid="project-tag-form" onSubmit={addTag}>
        <select
          data-testid="project-tag-add"
          value={tagToAdd}
          onChange={(e) => setTagToAdd(e.target.value)}
        >
          <option value="">Add a label</option>
          {allTags
            .filter((t) => !tags.some((mine) => mine.id === t.id))
            .map((t) => (
              <option key={t.id} value={String(t.id)}>
                {t.name}
              </option>
            ))}
        </select>
        <button data-testid="project-tag-add-submit" type="submit">
          Add label
        </button>
      </form>
    </>
  );
}

// A project's tasks, filterable by whether they are still open or already done, each with a control
// to tick it off or reopen it. Owns its own task state, scoped to the one project.
function TasksPanel({ projectId }: { projectId: number }) {
  const [tasks, reload] = useJsonResource<Task[]>(`/api/tasks?projectId=${projectId}`, []);
  const [taskFilter, setTaskFilter] = useState<'ALL' | 'OPEN' | 'DONE'>('ALL');

  async function toggleTask(id: number) {
    await fetch(`/api/tasks/${id}/toggle`, { method: 'POST' });
    await reload();
  }

  const shownTasks = tasks.filter((t) =>
    taskFilter === 'OPEN' ? !t.done : taskFilter === 'DONE' ? t.done : true,
  );

  return (
    <>
      <select
        data-testid="task-filter"
        value={taskFilter}
        onChange={(e) => setTaskFilter(e.target.value as 'ALL' | 'OPEN' | 'DONE')}
      >
        <option value="ALL">All tasks</option>
        <option value="OPEN">Open</option>
        <option value="DONE">Done</option>
      </select>

      <table data-testid="project-tasks-table">
        <tbody>
          {shownTasks.map((t) => (
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
    </>
  );
}

// The notes jotted against one target (a job or an invoice): a running list, newest first, and a
// form to add another. Polymorphic by targetType + targetId so the same panel serves any entity;
// the wrapper's data-testid names which surface it is (e.g. project-notes, invoice-notes). Owns its
// own note state, scoped to the one target it is showing.
function NotesPanel({
  targetType,
  targetId,
  wrapperTestId,
}: {
  targetType: string;
  targetId: number;
  wrapperTestId: string;
}) {
  const [notes, reload] = useJsonResource<Note[]>(
    `/api/notes?targetType=${targetType}&targetId=${targetId}`,
    [],
  );
  const [noteText, setNoteText] = useState('');

  async function submitNote(e: React.FormEvent) {
    e.preventDefault();
    if (!noteText.trim()) {
      return;
    }
    await fetch('/api/notes', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ targetType, targetId, text: noteText }),
    });
    setNoteText('');
    await reload();
  }

  return (
    <section data-testid={wrapperTestId}>
      <form data-testid="note-form" onSubmit={submitNote}>
        <input
          data-testid="note-form-text"
          placeholder="Write a note"
          value={noteText}
          onChange={(e) => setNoteText(e.target.value)}
        />
        <button data-testid="note-form-submit" type="submit">
          Add note
        </button>
      </form>

      <ul data-testid={`${wrapperTestId}-list`}>
        {notes.map((n) => (
          <li key={n.id} data-testid={`note-row-${n.id}`}>
            <span data-testid="note-text">{n.text}</span>
          </li>
        ))}
      </ul>
    </section>
  );
}

// A project's detail: its budget, labels, tasks and notes, plus the invoices raised against it. Each
// is a self-contained panel that owns its own state; this component just composes them and keeps the
// budget's derived figures in step with the invoices — a new invoice, a status move or an edited line
// item bumps `invoicesVersion`, which re-reads the budget.
function ProjectDetail({ project }: { project: Project }) {
  const [invoicesVersion, setInvoicesVersion] = useState(0);
  // Which invoice (if any) is drilled into. Held here rather than inside ProjectInvoices so the
  // project-level panels can step aside while an invoice's own detail — including its notes — is on
  // screen; that also keeps the shared note-form anchors unambiguous (only the open target's form is
  // present at a time).
  const [openInvoiceId, setOpenInvoiceId] = useState<number | null>(null);

  return (
    <section data-testid="project-detail">
      <h2 data-testid="project-detail-name">{project.name}</h2>
      {openInvoiceId === null && (
        <>
          <BudgetPanel projectId={project.id} reloadKey={invoicesVersion} />
          <TagsPanel projectId={project.id} />
          <TasksPanel projectId={project.id} />
          <NotesPanel targetType="project" targetId={project.id} wrapperTestId="project-notes" />
        </>
      )}
      <ProjectInvoices
        projectId={project.id}
        onInvoicesChanged={() => setInvoicesVersion((v) => v + 1)}
        openInvoiceId={openInvoiceId}
        setOpenInvoiceId={setOpenInvoiceId}
      />
    </section>
  );
}

type ProjectTagLink = { projectId: number; tagId: number };

// The project list: a row per project showing its client's name and status, with controls to open,
// archive or delete it.
function ProjectsTable({
  projects,
  onOpen,
  onArchive,
  onDelete,
}: {
  projects: Project[];
  onOpen: (id: number) => void;
  onArchive: (id: number) => void;
  onDelete: (id: number) => void;
}) {
  return (
    <table data-testid="projects-table">
      <tbody>
        {projects.map((p) => (
          <tr key={p.id} data-testid={`project-row-${p.id}`}>
            <td data-testid="project-name">{p.name}</td>
            <td data-testid="project-code">{p.code}</td>
            <td data-testid="project-client">{p.clientName}</td>
            <td data-testid="project-status">{p.status}</td>
            <td>
              <button data-testid={`project-open-${p.id}`} onClick={() => onOpen(p.id)}>
                Open
              </button>
              <button data-testid={`project-archive-${p.id}`} onClick={() => onArchive(p.id)}>
                Archive
              </button>
              <button data-testid={`project-delete-${p.id}`} onClick={() => onDelete(p.id)}>
                Delete
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

function ProjectsPage() {
  const [projects, reloadProjects] = useJsonResource<Project[]>('/api/projects', []);
  const [clients] = useJsonResource<Client[]>('/api/clients', []);
  const [tags] = useJsonResource<Tag[]>('/api/tags', []);
  const [tagLinks] = useJsonResource<ProjectTagLink[]>('/api/project-tags', []);
  const [tagFilter, setTagFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [name, setName] = useState('');
  const [code, setCode] = useState('');
  const [codeError, setCodeError] = useState('');
  const [clientId, setClientId] = useState('');
  const [status, setStatus] = useState<ProjectStatus>('ACTIVE');
  const [openId, setOpenId] = useState<number | null>(null);
  // Archived projects are tucked away (retained, not deleted) and hidden by default; this toggle
  // reveals them again.
  const [showArchived, setShowArchived] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setCodeError('');
    if (!name.trim() || !clientId || !code.trim()) {
      return;
    }
    const res = await fetch('/api/projects', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, code, clientId: Number(clientId), status }),
    });
    // A code must be unique across jobs; the server rejects a duplicate, which surfaces here as a
    // field error rather than a new row.
    if (!res.ok) {
      setCodeError('That code is already in use');
      return;
    }
    setName('');
    setCode('');
    setClientId('');
    setStatus('ACTIVE');
    await reloadProjects();
  }

  // Delete and archive are the same shape — POST the action, drop the open detail if it was showing
  // this project, then refresh the list — so they share one call site.
  async function act(id: number, action: 'delete' | 'archive') {
    await fetch(`/api/projects/${id}/${action}`, { method: 'POST' });
    if (openId === id) {
      setOpenId(null);
    }
    await reloadProjects();
  }

  // Narrow the list one step at a time: hide archived unless asked, keep only projects carrying the
  // chosen label, then keep only the chosen status. An empty filter leaves that step wide open.
  const filterTagId = tagFilter ? Number(tagFilter) : null;
  const visible = projects
    .filter((p) => showArchived || !p.archived)
    .filter(
      (p) =>
        filterTagId === null ||
        tagLinks.some((l) => l.projectId === p.id && l.tagId === filterTagId),
    )
    .filter((p) => !statusFilter || p.status === statusFilter);
  const open = projects.find((p) => p.id === openId) ?? null;

  return (
    <section data-testid="projects">
      <form data-testid="project-form" onSubmit={submit}>
        <input
          data-testid="project-form-name"
          placeholder="Job name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <input
          data-testid="project-form-code"
          placeholder="Reference code"
          value={code}
          onChange={(e) => setCode(e.target.value)}
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
        <select
          data-testid="project-form-status"
          value={status}
          onChange={(e) => setStatus(e.target.value as ProjectStatus)}
        >
          <option value="ACTIVE">Active</option>
          <option value="ON_HOLD">On hold</option>
          <option value="FINISHED">Finished</option>
        </select>
        <button data-testid="project-form-submit" type="submit">
          Add job
        </button>
        {codeError && (
          <p data-testid="project-form-code-error" role="alert">
            {codeError}
          </p>
        )}
      </form>

      <button
        data-testid="projects-show-archived"
        type="button"
        onClick={() => setShowArchived((v) => !v)}
      >
        {showArchived ? 'Hide archived' : 'Show archived'}
      </button>

      <select
        data-testid="project-tag-filter"
        value={tagFilter}
        onChange={(e) => setTagFilter(e.target.value)}
      >
        <option value="">All labels</option>
        {tags.map((t) => (
          <option key={t.id} value={String(t.id)}>
            {t.name}
          </option>
        ))}
      </select>

      <select
        data-testid="project-status-filter"
        value={statusFilter}
        onChange={(e) => setStatusFilter(e.target.value)}
      >
        <option value="">All statuses</option>
        <option value="ACTIVE">Active</option>
        <option value="ON_HOLD">On hold</option>
        <option value="FINISHED">Finished</option>
      </select>

      {projects.length === 0 ? (
        <p data-testid="projects-empty">No jobs yet.</p>
      ) : (
        <ProjectsTable
          projects={visible}
          onOpen={setOpenId}
          onArchive={(id) => act(id, 'archive')}
          onDelete={(id) => act(id, 'delete')}
        />
      )}

      {open && <ProjectDetail key={open.id} project={open} />}
    </section>
  );
}

export const feature: Feature = { id: 'projects', label: 'Jobs', Page: ProjectsPage };
