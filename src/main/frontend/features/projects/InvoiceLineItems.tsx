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
  return `$${Number(amount).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
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
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editDescription, setEditDescription] = useState('');
  const [editQty, setEditQty] = useState('');
  const [editUnitPrice, setEditUnitPrice] = useState('');
  const [editError, setEditError] = useState('');

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

  function startEdit(li: LineItem) {
    setEditingId(li.id);
    setEditDescription(li.description);
    setEditQty(String(li.quantity));
    setEditUnitPrice(String(li.unitPrice));
    setEditError('');
  }

  function cancelEdit() {
    setEditingId(null);
    setEditError('');
  }

  async function saveEdit(lineItemId: number) {
    const quantity = Number(editQty);
    const price = Number(editUnitPrice);
    if (!editDescription.trim() || !(quantity > 0) || !(price >= 0)) {
      setEditError('A description, a quantity and a price are required');
      return;
    }
    setEditError('');
    const res = await fetch(`/api/invoices/${invoiceId}/lineitems/${lineItemId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ description: editDescription, quantity, unitPrice: price }),
    });
    if (!res.ok) {
      return;
    }
    setEditingId(null);
    await load();
  }

  async function remove(lineItemId: number) {
    const res = await fetch(`/api/invoices/${invoiceId}/lineitems/${lineItemId}`, {
      method: 'DELETE',
    });
    if (!res.ok) {
      return;
    }
    if (editingId === lineItemId) {
      setEditingId(null);
    }
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
            <th></th>
          </tr>
        </thead>
        <tbody>
          {lineItems.map((li) =>
            editingId === li.id ? (
              <tr key={li.id} data-testid={`lineitem-row-${li.id}`}>
                <td>
                  <input
                    data-testid="lineitem-edit-description"
                    placeholder="Description"
                    value={editDescription}
                    onChange={(e) => setEditDescription(e.target.value)}
                  />
                </td>
                <td>
                  <input
                    data-testid="lineitem-edit-qty"
                    placeholder="How many"
                    value={editQty}
                    onChange={(e) => setEditQty(e.target.value)}
                  />
                </td>
                <td>
                  <input
                    data-testid="lineitem-edit-unitprice"
                    placeholder="Price each"
                    value={editUnitPrice}
                    onChange={(e) => setEditUnitPrice(e.target.value)}
                  />
                </td>
                <td data-testid="lineitem-total">
                  {formatAmount(Number(editQty || 0) * Number(editUnitPrice || 0))}
                </td>
                <td>
                  <button
                    type="button"
                    data-testid={`lineitem-save-${li.id}`}
                    onClick={() => void saveEdit(li.id)}
                  >
                    Save
                  </button>
                  <button
                    type="button"
                    data-testid={`lineitem-cancel-${li.id}`}
                    onClick={cancelEdit}
                  >
                    Cancel
                  </button>
                  {editError ? (
                    <p data-testid="lineitem-edit-error" role="alert">
                      {editError}
                    </p>
                  ) : null}
                </td>
              </tr>
            ) : (
              <tr key={li.id} data-testid={`lineitem-row-${li.id}`}>
                <td data-testid="lineitem-description">{li.description}</td>
                <td data-testid="lineitem-qty">{li.quantity}</td>
                <td data-testid="lineitem-unitprice">{formatAmount(li.unitPrice)}</td>
                <td data-testid="lineitem-total">
                  {formatAmount(Number(li.quantity) * Number(li.unitPrice))}
                </td>
                <td>
                  <button
                    type="button"
                    data-testid={`lineitem-edit-${li.id}`}
                    onClick={() => startEdit(li)}
                  >
                    Change
                  </button>
                  <button
                    type="button"
                    data-testid={`lineitem-remove-${li.id}`}
                    onClick={() => void remove(li.id)}
                  >
                    Remove
                  </button>
                </td>
              </tr>
            ),
          )}
        </tbody>
        <tfoot>
          <tr>
            <td colSpan={3}>Total</td>
            <td data-testid="invoice-amount">{formatAmount(total)}</td>
            <td></td>
          </tr>
        </tfoot>
      </table>
    </section>
  );
}
