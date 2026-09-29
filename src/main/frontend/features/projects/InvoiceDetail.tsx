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
  return `$${Number(n).toFixed(2)}`;
}

export function InvoiceDetail({ invoiceId }: { invoiceId: number }) {
  const [lineItems, setLineItems] = useState<LineItem[]>([]);
  const [description, setDescription] = useState('');
  const [qty, setQty] = useState('');
  const [unitPrice, setUnitPrice] = useState('');

  async function load() {
    const res = await fetch(`/api/lineitems?invoiceId=${invoiceId}`);
    if (res.ok) {
      setLineItems(await res.json());
    }
  }

  useEffect(() => {
    void load();
  }, [invoiceId]);

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

  const total = lineItems.reduce((sum, li) => sum + li.qty * Number(li.unitPrice), 0);

  return (
    <section data-testid="invoice-detail">
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
          </tr>
        </thead>
        <tbody>
          {lineItems.map((li) => (
            <tr key={li.id} data-testid={`lineitem-row-${li.id}`}>
              <td data-testid="lineitem-description">{li.description}</td>
              <td data-testid="lineitem-qty">{li.qty}</td>
              <td data-testid="lineitem-unitprice">{money(li.unitPrice)}</td>
              <td data-testid="lineitem-amount">{money(li.qty * Number(li.unitPrice))}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <p>
        Total: <span data-testid="invoice-amount">{money(total)}</span>
      </p>
    </section>
  );
}
