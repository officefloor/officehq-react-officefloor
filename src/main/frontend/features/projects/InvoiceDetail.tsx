import React, { useEffect, useState } from 'react';
import { InvoiceNotes } from './InvoiceNotes';

// The detail of a single invoice, shown when an invoice is opened from the project's invoice list.
// Owns its own state (CLAUDE.md — features own their state). Lists the invoice's line items — the
// things being charged for (description, quantity, unit price) — a derived total (the sum of each
// line's qty * unit price), and a form to add another line. Amounts render with 2 decimals.
type LineItem = {
  id: number;
  invoiceId: number;
  description: string;
  qty: number;
  unit: string;
  unitPrice: number;
};

// The invoice's money is shown in the client's currency (e.g. USD -> "$100.00", EUR -> "€100.00").
function money(n: number, currency = 'USD'): string {
  return Number(n).toLocaleString('en-US', { style: 'currency', currency });
}

type EditDraft = { description: string; qty: string; unit: string; unitPrice: string };

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
  const [unit, setUnit] = useState('');
  const [unitPrice, setUnitPrice] = useState('');
  const [editingId, setEditingId] = useState<number | null>(null);
  const [draft, setDraft] = useState<EditDraft>({
    description: '',
    qty: '',
    unit: '',
    unitPrice: '',
  });
  const [payments, setPayments] = useState<Payment[]>([]);
  const [paymentAmount, setPaymentAmount] = useState('');
  const [paymentDate, setPaymentDate] = useState('');
  const [status, setStatus] = useState('');
  const [discountPct, setDiscountPct] = useState(0);
  const [taxPct, setTaxPct] = useState(0);
  const [currency, setCurrency] = useState('USD');

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
  // re-read it whenever the payments change rather than tracking it by hand. The invoice also
  // carries its discount and tax percentages, which we read here to show the discount, the tax and
  // the final total.
  async function loadStatus() {
    const res = await fetch(`/api/invoices/get?id=${invoiceId}`);
    if (res.ok) {
      const inv: { status: string; discountPct: number; taxPct: number; currency: string } =
        await res.json();
      setStatus(inv.status);
      setDiscountPct(Number(inv.discountPct));
      setTaxPct(Number(inv.taxPct));
      setCurrency(inv.currency);
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
        unit,
        unitPrice: Number(unitPrice),
      }),
    });
    if (res.ok) {
      const created: LineItem = await res.json();
      setLineItems((prev) => [...prev, created]);
      setDescription('');
      setQty('');
      setUnit('');
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
      unit: li.unit,
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
        unit: draft.unit,
        unitPrice: Number(draft.unitPrice),
      }),
    });
    if (res.ok) {
      const updated: LineItem = await res.json();
      setLineItems((prev) => prev.map((li) => (li.id === id ? updated : li)));
      setEditingId(null);
    }
  }

  // The subtotal is the sum of the line items; the discount is that percentage taken off it. Sales
  // tax is then added on top, worked out on the discounted amount (subtotal minus discount). The
  // final total is the discounted amount plus the tax.
  const subtotal = lineItems.reduce((sum, li) => sum + li.qty * Number(li.unitPrice), 0);
  const discount = subtotal * (discountPct / 100);
  const tax = (subtotal - discount) * (taxPct / 100);
  const total = subtotal - discount + tax;

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
          data-testid="lineitem-form-unit"
          placeholder="Unit"
          value={unit}
          onChange={(e) => setUnit(e.target.value)}
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
            <th>Unit</th>
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
                    data-testid={`lineitem-edit-unit-${li.id}`}
                    value={draft.unit}
                    onChange={(e) => setDraft((d) => ({ ...d, unit: e.target.value }))}
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
                  {money(Number(draft.qty) * Number(draft.unitPrice), currency)}
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
                <td data-testid="lineitem-unit">{li.unit}</td>
                <td data-testid="lineitem-unitprice">{money(li.unitPrice, currency)}</td>
                <td data-testid="lineitem-amount">{money(li.qty * Number(li.unitPrice), currency)}</td>
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
        Subtotal: <span data-testid="invoice-subtotal">{money(subtotal, currency)}</span>
      </p>
      <p>
        Discount: <span data-testid="invoice-discount">{money(discount, currency)}</span>
      </p>
      <p>
        Tax: <span data-testid="invoice-tax">{money(tax, currency)}</span>
      </p>
      <p>
        Total: <span data-testid="invoice-amount">{money(total, currency)}</span>
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
              <td data-testid="payment-amount">{money(p.amount, currency)}</td>
              <td data-testid="payment-date">{p.date}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <InvoiceNotes invoiceId={invoiceId} />
    </section>
  );
}
