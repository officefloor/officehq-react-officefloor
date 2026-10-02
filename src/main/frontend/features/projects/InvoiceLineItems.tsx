import { useEffect, useState, type FormEvent } from 'react';
import { formatMoney } from '../../ui/money';
import { InvoicePayments } from './InvoicePayments';

// Opened from a project's invoice row (invoice-open-<id>): instead of typing one figure, you list
// the things you are charging for — each with a description, how many, and the price each. The
// invoice total is worked out for you as the sum of each line's quantity times unit price. Line
// items are scoped to the invoice via /api/invoices/<id>/line-items.
type LineItem = {
  id: number;
  invoiceId: number;
  description: string;
  qty: number;
  unit: string;
  unitPrice: number;
};

// The invoice's status is worked out from its payments (PAID once covered, PARTIAL once part paid),
// so we read it back from the server rather than flipping it by hand. It also carries the
// percentage discount taken off the subtotal (0 when there is none).
type Invoice = {
  id: number;
  status: string;
  discountPct: number;
};

export function InvoiceLineItems({
  invoiceId,
  onBack,
}: {
  invoiceId: number;
  onBack: () => void;
}) {
  const [lineItems, setLineItems] = useState<LineItem[]>([]);
  const [status, setStatus] = useState('');
  const [discountPct, setDiscountPct] = useState(0);
  const [description, setDescription] = useState('');
  const [qty, setQty] = useState('');
  const [unit, setUnit] = useState('');
  const [unitPrice, setUnitPrice] = useState('');

  async function load() {
    const res = await fetch(`/api/invoices/${invoiceId}/line-items`);
    if (res.ok) {
      setLineItems(await res.json());
    }
  }

  async function loadInvoice() {
    const res = await fetch(`/api/invoices/${invoiceId}`);
    if (res.ok) {
      const invoice: Invoice = await res.json();
      setStatus(invoice.status);
      setDiscountPct(Number(invoice.discountPct));
    }
  }

  useEffect(() => {
    void load();
    void loadInvoice();
  }, [invoiceId]);

  async function onRemove(lineItemId: number) {
    const res = await fetch(
      `/api/invoices/${invoiceId}/line-items/${lineItemId}/remove`,
      { method: 'POST' },
    );
    if (res.ok) {
      // The endpoint returns the invoice's remaining line items; the total re-derives from them.
      setLineItems(await res.json());
      // The amount changed, so the status worked out from the payments may have too.
      void loadInvoice();
    }
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    const res = await fetch(`/api/invoices/${invoiceId}/line-items`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        description,
        qty: Number(qty),
        unit,
        unitPrice: Number(unitPrice),
      }),
    });
    if (res.ok) {
      // The endpoint returns the invoice's line items, totals included on re-derive.
      setLineItems(await res.json());
      // The amount changed, so the status worked out from the payments may have too.
      void loadInvoice();
      setDescription('');
      setQty('');
      setUnit('');
      setUnitPrice('');
    }
  }

  // The subtotal is the sum of each line's quantity times unit price. The discount takes a
  // percentage off that subtotal, and the final total is what is left after it.
  const subtotal = lineItems.reduce((sum, item) => sum + item.qty * Number(item.unitPrice), 0);
  const discount = (subtotal * discountPct) / 100;
  const finalTotal = subtotal - discount;

  return (
    <section data-testid="invoice-detail">
      <button data-testid="invoice-close" type="button" onClick={onBack}>
        Back to invoices
      </button>

      <p data-testid="invoice-status">{status}</p>

      <table data-testid="invoice-lineitems-table">
        <thead>
          <tr>
            <th>Description</th>
            <th>Qty</th>
            <th>Unit</th>
            <th>Price each</th>
            <th>Amount</th>
          </tr>
        </thead>
        <tbody>
          {lineItems.map((item) => (
            <tr key={item.id} data-testid={`lineitem-row-${item.id}`}>
              <td data-testid="lineitem-description">{item.description}</td>
              <td data-testid="lineitem-qty">{item.qty}</td>
              <td data-testid="lineitem-unit">{item.unit}</td>
              <td data-testid="lineitem-unitprice">{formatMoney(item.unitPrice)}</td>
              <td data-testid="lineitem-amount">
                {formatMoney(item.qty * Number(item.unitPrice))}
              </td>
              <td>
                <button
                  data-testid={`lineitem-remove-${item.id}`}
                  type="button"
                  onClick={() => onRemove(item.id)}
                >
                  Remove
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

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
          placeholder="Price each"
          value={unitPrice}
          onChange={(e) => setUnitPrice(e.target.value)}
        />
        <button data-testid="lineitem-form-submit" type="submit">
          Add line item
        </button>
      </form>

      <dl data-testid="invoice-totals">
        <dt>Subtotal</dt>
        <dd data-testid="invoice-subtotal">{formatMoney(subtotal)}</dd>
        <dt>Discount</dt>
        <dd data-testid="invoice-discount">{formatMoney(discount)}</dd>
        <dt>Total</dt>
        <dd data-testid="invoice-amount">{formatMoney(finalTotal)}</dd>
      </dl>

      <InvoicePayments invoiceId={invoiceId} onPaymentRecorded={loadInvoice} />
    </section>
  );
}
