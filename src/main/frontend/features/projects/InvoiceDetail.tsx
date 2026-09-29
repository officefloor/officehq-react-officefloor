import React, { useEffect, useState } from 'react';

// The detail of a single invoice, shown when an invoice is opened from the project's invoice list.
// Owns its own state (CLAUDE.md — features own their state). Lists the invoice's line items — the
// things being charged for (description, quantity, unit price) — a derived total (the sum of each
// line's qty * unit price), and a form to add another line. Amounts render with 2 decimals.
type LineItem = {
  id: number;
  invoiceId: number;
  description: string;
  qty: number;
  unitPrice: number;
};

function money(n: number): string {
  return Number(n).toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}

type EditDraft = { description: string; qty: string; unitPrice: string };

// A payment a client has made against this invoice: how much (amount) and when (date, yyyy-MM-dd).
type Payment = {
  id: number;
  invoiceId: number;
  amount: number;
  date: string;
};

export function InvoiceDetail({ invoiceId }: { invoiceId: number }) {
  const [lineItems, setLineItems] = useState<LineItem[]>([]);
  const [description, setDescription] = useState('');
  const [qty, setQty] = useState('');
  const [unitPrice, setUnitPrice] = useState('');
  const [editingId, setEditingId] = useState<number | null>(null);
  const [draft, setDraft] = useState<EditDraft>({ description: '', qty: '', unitPrice: '' });
  const [payments, setPayments] = useState<Payment[]>([]);
  const [paymentAmount, setPaymentAmount] = useState('');
  const [paymentDate, setPaymentDate] = useState('');
  const [status, setStatus] = useState('');

  async function load() {
    const res = await fetch(`/api/lineitems?invoiceId=${invoiceId}`);
    if (res.ok) {
      setLineItems(await res.json());
    }
  }

  async function loadPayments() {
    const res = await fetch(`/api/payments?invoiceId=${invoiceId}`);
    if (res.ok) {
      setPayments(await res.json());
    }
  }

  // The invoice's status is worked out server-side from the payments recorded against it, so we
  // re-read it whenever the payments change rather than tracking it by hand.
  async function loadStatus() {
    const res = await fetch(`/api/invoices/get?id=${invoiceId}`);
    if (res.ok) {
      const inv: { status: string } = await res.json();
      setStatus(inv.status);
    }
  }

  useEffect(() => {
    void load();
    void loadPayments();
    void loadStatus();
  }, [invoiceId]);

  async function onRecordPayment(e: React.FormEvent) {
    e.preventDefault();
    const res = await fetch('/api/payments', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        invoiceId,
        amount: Number(paymentAmount),
        date: paymentDate,
      }),
    });
    if (res.ok) {
      const created: Payment = await res.json();
      setPayments((prev) => [...prev, created]);
      setPaymentAmount('');
      setPaymentDate('');
      void loadStatus();
    }
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    const res = await fetch('/api/lineitems', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        invoiceId,
        description,
        qty: Number(qty),
        unitPrice: Number(unitPrice),
      }),
    });
    if (res.ok) {
      const created: LineItem = await res.json();
      setLineItems((prev) => [...prev, created]);
      setDescription('');
      setQty('');
      setUnitPrice('');
    }
  }

  async function onRemove(id: number) {
    const res = await fetch('/api/lineitems/remove', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id }),
    });
    if (res.ok) {
      setLineItems((prev) => prev.filter((li) => li.id !== id));
      if (editingId === id) {
        setEditingId(null);
      }
    }
  }

  function startEdit(li: LineItem) {
    setEditingId(li.id);
    setDraft({
      description: li.description,
      qty: String(li.qty),
      unitPrice: String(li.unitPrice),
    });
  }

  async function onSaveEdit(id: number) {
    const res = await fetch('/api/lineitems/update', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        id,
        description: draft.description,
        qty: Number(draft.qty),
        unitPrice: Number(draft.unitPrice),
      }),
    });
    if (res.ok) {
      const updated: LineItem = await res.json();
      setLineItems((prev) => prev.map((li) => (li.id === id ? updated : li)));
      setEditingId(null);
    }
  }

  const total = lineItems.reduce((sum, li) => sum + li.qty * Number(li.unitPrice), 0);

  return (
    <section data-testid="invoice-detail">
      <p>
        Status: <span data-testid="invoice-status">{status}</span>
      </p>
      <h3>Line items</h3>
      <form data-testid="lineitem-form" onSubmit={onSubmit}>
        <input
          data-testid="lineitem-form-description"
          placeholder="Description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
        <input
          data-testid="lineitem-form-qty"
          placeholder="Qty"
          value={qty}
          onChange={(e) => setQty(e.target.value)}
        />
        <input
          data-testid="lineitem-form-unitprice"
          placeholder="Unit price"
          value={unitPrice}
          onChange={(e) => setUnitPrice(e.target.value)}
        />
        <button type="submit" data-testid="lineitem-form-submit">
          Add line item
        </button>
      </form>

      <table data-testid="invoice-lineitems-table">
        <thead>
          <tr>
            <th>Description</th>
            <th>Qty</th>
            <th>Unit price</th>
            <th>Amount</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {lineItems.map((li) =>
            editingId === li.id ? (
              <tr key={li.id} data-testid={`lineitem-row-${li.id}`}>
                <td>
                  <input
                    data-testid={`lineitem-edit-description-${li.id}`}
                    value={draft.description}
                    onChange={(e) => setDraft((d) => ({ ...d, description: e.target.value }))}
                  />
                </td>
                <td>
                  <input
                    data-testid={`lineitem-edit-qty-${li.id}`}
                    value={draft.qty}
                    onChange={(e) => setDraft((d) => ({ ...d, qty: e.target.value }))}
                  />
                </td>
                <td>
                  <input
                    data-testid={`lineitem-edit-unitprice-${li.id}`}
                    value={draft.unitPrice}
                    onChange={(e) => setDraft((d) => ({ ...d, unitPrice: e.target.value }))}
                  />
                </td>
                <td data-testid="lineitem-amount">
                  {money(Number(draft.qty) * Number(draft.unitPrice))}
                </td>
                <td>
                  <button
                    type="button"
                    data-testid={`lineitem-save-${li.id}`}
                    onClick={() => void onSaveEdit(li.id)}
                  >
                    Save
                  </button>
                  <button
                    type="button"
                    data-testid={`lineitem-cancel-${li.id}`}
                    onClick={() => setEditingId(null)}
                  >
                    Cancel
                  </button>
                </td>
              </tr>
            ) : (
              <tr key={li.id} data-testid={`lineitem-row-${li.id}`}>
                <td data-testid="lineitem-description">{li.description}</td>
                <td data-testid="lineitem-qty">{li.qty}</td>
                <td data-testid="lineitem-unitprice">{money(li.unitPrice)}</td>
                <td data-testid="lineitem-amount">{money(li.qty * Number(li.unitPrice))}</td>
                <td>
                  <button
                    type="button"
                    data-testid={`lineitem-edit-${li.id}`}
                    onClick={() => startEdit(li)}
                  >
                    Edit
                  </button>
                  <button
                    type="button"
                    data-testid={`lineitem-remove-${li.id}`}
                    onClick={() => void onRemove(li.id)}
                  >
                    Remove
                  </button>
                </td>
              </tr>
            ),
          )}
        </tbody>
      </table>

      <p>
        Total: <span data-testid="invoice-amount">{money(total)}</span>
      </p>

      <h3>Payments</h3>
      <form data-testid="payment-form" onSubmit={onRecordPayment}>
        <input
          data-testid="payment-form-amount"
          placeholder="Amount"
          value={paymentAmount}
          onChange={(e) => setPaymentAmount(e.target.value)}
        />
        <input
          data-testid="payment-form-date"
          placeholder="Date"
          value={paymentDate}
          onChange={(e) => setPaymentDate(e.target.value)}
        />
        <button type="submit" data-testid="payment-form-submit">
          Record payment
        </button>
      </form>

      <table data-testid="invoice-payments-table">
        <thead>
          <tr>
            <th>Amount</th>
            <th>Date</th>
          </tr>
        </thead>
        <tbody>
          {payments.map((p) => (
            <tr key={p.id} data-testid={`payment-row-${p.id}`}>
              <td data-testid="payment-amount">{money(p.amount)}</td>
              <td data-testid="payment-date">{p.date}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
