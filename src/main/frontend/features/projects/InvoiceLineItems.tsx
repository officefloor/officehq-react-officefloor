import React, { useEffect, useState } from 'react';

// An opened invoice's line items, rendered inside the projects feature. The owner lists the things
// being charged for — each with a description, how many, and the price each — and the invoice total
// is worked out for them (the sum of each line's quantity times unit price). Owns its own state and
// data loading (no global store); composed, not branched.
type LineItem = {
  id: number;
  invoiceId: number;
  description: string;
  quantity: number;
  unitPrice: number;
};

function formatAmount(amount: number): string {
  return `$${Number(amount).toFixed(2)}`;
}

export function InvoiceLineItems({
  invoiceId,
  onClose,
}: {
  invoiceId: number;
  onClose: () => void;
}) {
  const [lineItems, setLineItems] = useState<LineItem[]>([]);
  const [description, setDescription] = useState('');
  const [qty, setQty] = useState('');
  const [unitPrice, setUnitPrice] = useState('');
  const [error, setError] = useState('');

  async function load() {
    const res = await fetch(`/api/invoices/${invoiceId}/lineitems`);
    setLineItems(await res.json());
  }

  useEffect(() => {
    void load();
  }, [invoiceId]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    const quantity = Number(qty);
    const price = Number(unitPrice);
    if (!description.trim() || !(quantity > 0) || !(price >= 0)) {
      setError('A description, a quantity and a price are required');
      return;
    }
    setError('');
    const res = await fetch(`/api/invoices/${invoiceId}/lineitems`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ description, quantity, unitPrice: price }),
    });
    if (!res.ok) {
      return;
    }
    setDescription('');
    setQty('');
    setUnitPrice('');
    await load();
  }

  const total = lineItems.reduce((sum, li) => sum + Number(li.quantity) * Number(li.unitPrice), 0);

  return (
    <section data-testid="invoice-lineitems">
      <h2>Invoice line items</h2>

      <button type="button" data-testid="invoice-close" onClick={onClose}>
        Back to invoices
      </button>

      <form data-testid="lineitem-form" onSubmit={onSubmit}>
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
        <button type="submit" data-testid="lineitem-form-submit">
          Add line item
        </button>
        {error ? (
          <p data-testid="lineitem-form-error" role="alert">
            {error}
          </p>
        ) : null}
      </form>

      <table data-testid="invoice-lineitems-table">
        <thead>
          <tr>
            <th>Description</th>
            <th>How many</th>
            <th>Price each</th>
            <th>Line total</th>
          </tr>
        </thead>
        <tbody>
          {lineItems.map((li) => (
            <tr key={li.id} data-testid={`lineitem-row-${li.id}`}>
              <td data-testid="lineitem-description">{li.description}</td>
              <td data-testid="lineitem-qty">{li.quantity}</td>
              <td data-testid="lineitem-unitprice">{formatAmount(li.unitPrice)}</td>
              <td data-testid="lineitem-total">
                {formatAmount(Number(li.quantity) * Number(li.unitPrice))}
              </td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr>
            <td colSpan={3}>Total</td>
            <td data-testid="invoice-amount">{formatAmount(total)}</td>
          </tr>
        </tfoot>
      </table>
    </section>
  );
}
