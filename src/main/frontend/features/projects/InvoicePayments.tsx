import { useEffect, useState, type FormEvent } from 'react';
import { formatMoney } from '../../ui/money';

// Shown inside an opened invoice: the payments a client has made against it. Each payment records
// how much was paid and the date it was paid. You can record a new payment with an amount and a
// date. Payments are scoped to the invoice via /api/invoices/<id>/payments.
type Payment = {
  id: number;
  invoiceId: number;
  amount: number;
  date: string;
};

export function InvoicePayments({ invoiceId }: { invoiceId: number }) {
  const [payments, setPayments] = useState<Payment[]>([]);
  const [amount, setAmount] = useState('');
  const [date, setDate] = useState('');

  async function load() {
    const res = await fetch(`/api/invoices/${invoiceId}/payments`);
    if (res.ok) {
      setPayments(await res.json());
    }
  }

  useEffect(() => {
    void load();
  }, [invoiceId]);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    const res = await fetch(`/api/invoices/${invoiceId}/payments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ amount: Number(amount), date }),
    });
    if (res.ok) {
      // The endpoint returns the invoice's payments, including the one just recorded.
      setPayments(await res.json());
      setAmount('');
      setDate('');
    }
  }

  return (
    <section data-testid="invoice-payments">
      <h3>Payments</h3>

      <table data-testid="invoice-payments-table">
        <thead>
          <tr>
            <th>Amount</th>
            <th>Date</th>
          </tr>
        </thead>
        <tbody>
          {payments.map((payment) => (
            <tr key={payment.id} data-testid={`payment-row-${payment.id}`}>
              <td data-testid="payment-amount">{formatMoney(payment.amount)}</td>
              <td data-testid="payment-date">{payment.date}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <form data-testid="payment-form" onSubmit={onSubmit}>
        <input
          data-testid="payment-form-amount"
          placeholder="Amount"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
        />
        <input
          data-testid="payment-form-date"
          placeholder="Date"
          value={date}
          onChange={(e) => setDate(e.target.value)}
        />
        <button data-testid="payment-form-submit" type="submit">
          Record payment
        </button>
      </form>
    </section>
  );
}
