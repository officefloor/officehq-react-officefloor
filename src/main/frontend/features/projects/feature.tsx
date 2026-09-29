import React, { useEffect, useState } from 'react';
import type { Feature } from '../../router/routes';

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
};
type Task = { id: number; projectId: number; title: string; done: boolean };
type Tag = { id: number; name: string };
type Note = { id: number; targetType: string; targetId: number; text: string; at: string };

type LineItem = {
  id: number;
  invoiceId: number;
  description: string;
  qty: number;
  unitPrice: number;
};

type Payment = { id: number; invoiceId: number; amount: number; date: string };

function money(amount: number): string {
  return `$${Number(amount).toFixed(2)}`;
}

// One charge line while it is being edited on screen: the fields are held as raw strings so the
// inputs stay controlled and the total can recompute live as they change.
type EditItem = { id: number; description: string; qty: string; unitPrice: string };

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
  const [unitPrice, setUnitPrice] = useState('');
  const [payments, setPayments] = useState<Payment[]>([]);
  const [paymentAmount, setPaymentAmount] = useState('');
  const [paymentDate, setPaymentDate] = useState('');
  const [status, setStatus] = useState('');

  // The status is worked out from the payments recorded against the invoice, so re-read it whenever
  // a payment is added rather than flipping it by hand.
  async function loadStatus() {
    const res = await fetch(`/api/invoices/${invoiceId}`);
    const inv: Invoice = await res.json();
    setStatus(inv.status);
  }

  async function load() {
    const res = await fetch(`/api/invoices/${invoiceId}/line-items`);
    const rows: LineItem[] = await res.json();
    setItems(
      rows.map((li) => ({
        id: li.id,
        description: li.description,
        qty: String(li.qty),
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

  function editField(id: number, field: 'description' | 'qty' | 'unitPrice', value: string) {
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
        unitPrice: Number(unitPrice),
      }),
    });
    setDescription('');
    setQty('');
    setUnitPrice('');
    await load();
    onChange();
  }

  const total = items.reduce((sum, li) => sum + amountOf(li), 0);

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
              <td>
                <input
                  data-testid="lineitem-qty"
                  value={li.qty}
                  onChange={(e) => editField(li.id, 'qty', e.target.value)}
                />
              </td>
              <td>
                <input
                  data-testid="lineitem-unitprice"
                  value={li.unitPrice}
                  onChange={(e) => editField(li.id, 'unitPrice', e.target.value)}
                />
              </td>
              <td data-testid="lineitem-amount">{money(amountOf(li))}</td>
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

      <p data-testid="invoice-amount">{money(total)}</p>

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
              <td data-testid="payment-amount">{money(p.amount)}</td>
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
    </section>
  );
}

// A project's detail: its invoices, what they add up to, and a form to add a new invoice. Owns its
// own invoice state, scoped to the one project it is showing.
function ProjectDetail({ project }: { project: Project }) {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [allTags, setAllTags] = useState<Tag[]>([]);
  const [tagToAdd, setTagToAdd] = useState('');
  const [taskFilter, setTaskFilter] = useState<'ALL' | 'OPEN' | 'DONE'>('ALL');
  const [amount, setAmount] = useState('');
  const [amountError, setAmountError] = useState('');
  const [sortByDue, setSortByDue] = useState(false);
  const [openInvoiceId, setOpenInvoiceId] = useState<number | null>(null);
  const [notes, setNotes] = useState<Note[]>([]);
  const [noteText, setNoteText] = useState('');

  async function load() {
    const res = await fetch(`/api/invoices?projectId=${project.id}`);
    setInvoices(await res.json());
  }

  async function loadNotes() {
    const res = await fetch(`/api/notes?targetType=project&targetId=${project.id}`);
    setNotes(await res.json());
  }

  async function submitNote(e: React.FormEvent) {
    e.preventDefault();
    if (!noteText.trim()) {
      return;
    }
    await fetch('/api/notes', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ targetType: 'project', targetId: project.id, text: noteText }),
    });
    setNoteText('');
    await loadNotes();
  }

  async function loadTasks() {
    const res = await fetch(`/api/tasks?projectId=${project.id}`);
    setTasks(await res.json());
  }

  async function loadTags() {
    const [mine, all] = await Promise.all([
      fetch(`/api/projects/${project.id}/tags`).then((r) => r.json()),
      fetch('/api/tags').then((r) => r.json()),
    ]);
    setTags(mine);
    setAllTags(all);
  }

  useEffect(() => {
    void load();
    void loadTasks();
    void loadTags();
    void loadNotes();
  }, [project.id]);

  async function toggleTask(id: number) {
    await fetch(`/api/tasks/${id}/toggle`, { method: 'POST' });
    await loadTasks();
  }

  async function addTag(e: React.FormEvent) {
    e.preventDefault();
    if (!tagToAdd) {
      return;
    }
    await fetch(`/api/projects/${project.id}/tags`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ tagId: Number(tagToAdd) }),
    });
    setTagToAdd('');
    await loadTags();
  }

  async function removeTag(id: number) {
    await fetch(`/api/projects/${project.id}/tags/${id}/remove`, { method: 'POST' });
    await loadTags();
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

  const shownTasks = tasks.filter((t) =>
    taskFilter === 'OPEN' ? !t.done : taskFilter === 'DONE' ? t.done : true,
  );

  const total = invoices.reduce((sum, inv) => sum + Number(inv.amount), 0);
  const shownInvoices = sortByDue
    ? [...invoices].sort((a, b) => a.dueDate.localeCompare(b.dueDate))
    : invoices;

  return (
    <section data-testid="project-detail">
      <h2 data-testid="project-detail-name">{project.name}</h2>

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

      <section data-testid="project-notes">
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

        <ul data-testid="project-notes-list">
          {notes.map((n) => (
            <li key={n.id} data-testid={`note-row-${n.id}`}>
              <span data-testid="note-text">{n.text}</span>
            </li>
          ))}
        </ul>
      </section>

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
                  <td data-testid="invoice-amount">{money(inv.amount)}</td>
                  <td data-testid="invoice-due-amount">{money(inv.amountDue)}</td>
                  <td data-testid="invoice-issued">{inv.issuedDate}</td>
                  <td data-testid="invoice-due">{inv.dueDate}</td>
                  <td data-testid="invoice-status">{inv.status}</td>
                  <td>
                    <button
                      data-testid={`invoice-open-${inv.id}`}
                      onClick={() => setOpenInvoiceId(inv.id)}
                    >
                      Open
                    </button>
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
        </>
      ) : (
        <InvoiceDetail
          invoiceId={openInvoiceId}
          onChange={load}
          onClose={() => setOpenInvoiceId(null)}
        />
      )}
    </section>
  );
}

type ProjectTagLink = { projectId: number; tagId: number };

function ProjectsPage() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [clients, setClients] = useState<Client[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [tagLinks, setTagLinks] = useState<ProjectTagLink[]>([]);
  const [tagFilter, setTagFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [name, setName] = useState('');
  const [clientId, setClientId] = useState('');
  const [status, setStatus] = useState<ProjectStatus>('ACTIVE');
  const [openId, setOpenId] = useState<number | null>(null);
  // Archived projects are tucked away (retained, not deleted) and hidden by default; this toggle
  // reveals them again.
  const [showArchived, setShowArchived] = useState(false);

  async function loadProjects() {
    const res = await fetch('/api/projects');
    setProjects(await res.json());
  }

  async function loadClients() {
    const res = await fetch('/api/clients');
    setClients(await res.json());
  }

  async function loadTags() {
    const [all, links] = await Promise.all([
      fetch('/api/tags').then((r) => r.json()),
      fetch('/api/project-tags').then((r) => r.json()),
    ]);
    setTags(all);
    setTagLinks(links);
  }

  useEffect(() => {
    void loadProjects();
    void loadClients();
    void loadTags();
  }, []);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!name.trim() || !clientId) {
      return;
    }
    await fetch('/api/projects', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, clientId: Number(clientId), status }),
    });
    setName('');
    setClientId('');
    setStatus('ACTIVE');
    await loadProjects();
  }

  async function remove(id: number) {
    await fetch(`/api/projects/${id}/delete`, { method: 'POST' });
    if (openId === id) {
      setOpenId(null);
    }
    await loadProjects();
  }

  async function archive(id: number) {
    await fetch(`/api/projects/${id}/archive`, { method: 'POST' });
    if (openId === id) {
      setOpenId(null);
    }
    await loadProjects();
  }

  const open = projects.find((p) => p.id === openId) ?? null;
  const byArchived = showArchived ? projects : projects.filter((p) => !p.archived);
  const filterTagId = tagFilter ? Number(tagFilter) : null;
  const byTag =
    filterTagId === null
      ? byArchived
      : byArchived.filter((p) =>
          tagLinks.some((l) => l.projectId === p.id && l.tagId === filterTagId),
        );
  const visible = statusFilter ? byTag.filter((p) => p.status === statusFilter) : byTag;

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
          Add project
        </button>
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
        <p data-testid="projects-empty">No projects yet.</p>
      ) : (
        <table data-testid="projects-table">
          <tbody>
            {visible.map((p) => (
              <tr key={p.id} data-testid={`project-row-${p.id}`}>
                <td data-testid="project-name">{p.name}</td>
                <td data-testid="project-client">{p.clientName}</td>
                <td data-testid="project-status">{p.status}</td>
                <td>
                  <button data-testid={`project-open-${p.id}`} onClick={() => setOpenId(p.id)}>
                    Open
                  </button>
                  <button data-testid={`project-archive-${p.id}`} onClick={() => archive(p.id)}>
                    Archive
                  </button>
                  <button data-testid={`project-delete-${p.id}`} onClick={() => remove(p.id)}>
                    Delete
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
