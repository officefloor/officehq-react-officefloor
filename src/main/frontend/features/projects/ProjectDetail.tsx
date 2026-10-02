import { useEffect, useState, type FormEvent } from 'react';
import { formatMoney } from '../../ui/money';

// Opened from the projects list: a project's detail view lists ITS invoices, shows the derived
// total, and lets you add a new invoice for an amount. Invoices are scoped to the project via
// /api/projects/<id>/invoices. Money renders with exactly two decimals.
type Invoice = { id: number; projectId: number; amount: number; status: string };

export function ProjectDetail({ projectId }: { projectId: number }) {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [amount, setAmount] = useState('');
  const [amountError, setAmountError] = useState(false);

  async function load() {
    const res = await fetch(`/api/projects/${projectId}/invoices`);
    if (res.ok) {
      setInvoices(await res.json());
    }
  }

  useEffect(() => {
    void load();
  }, [projectId]);

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

  async function onPay(invoiceId: number) {
    const res = await fetch(`/api/invoices/${invoiceId}/pay`, { method: 'POST' });
    if (res.ok) {
      await load();
    }
  }

  const total = invoices.reduce((sum, invoice) => sum + Number(invoice.amount), 0);

  return (
    <section data-testid="project-detail">
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

      <table data-testid="project-invoices-table">
        <thead>
          <tr>
            <th>Amount</th>
            <th>Status</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {invoices.map((invoice) => (
            <tr key={invoice.id} data-testid={`invoice-row-${invoice.id}`}>
              <td data-testid="invoice-amount">{formatMoney(invoice.amount)}</td>
              <td data-testid="invoice-status">{invoice.status}</td>
              <td>
                {invoice.status !== 'PAID' && (
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
    </section>
  );
}
