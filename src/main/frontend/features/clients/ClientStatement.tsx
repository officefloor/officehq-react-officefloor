import { useEffect, useState } from 'react';
import { formatMoney } from '../../ui/money';

// A statement for a client: all of that client's invoices gathered in one place, with the total
// they still owe (the sum of what is due across their invoices). Scoped to the client via
// /api/clients/<id>/statement. Opened on demand from the client detail view.
type StatementInvoice = {
  id: number;
  projectName: string;
  amount: number;
  status: string;
  due: number;
};
type Statement = {
  invoices: StatementInvoice[];
  outstandingTotal: number;
};

export function ClientStatement({ clientId }: { clientId: number }) {
  const [statement, setStatement] = useState<Statement>({ invoices: [], outstandingTotal: 0 });

  useEffect(() => {
    async function load() {
      const res = await fetch(`/api/clients/${clientId}/statement`);
      if (res.ok) {
        setStatement(await res.json());
      }
    }
    void load();
  }, [clientId]);

  return (
    <section data-testid="client-statement">
      <h2>Statement</h2>
      <table data-testid="client-statement-table">
        <thead>
          <tr>
            <th>Invoice</th>
            <th>Project</th>
            <th>Amount</th>
            <th>Status</th>
            <th>Due</th>
          </tr>
        </thead>
        <tbody>
          {statement.invoices.map((invoice) => (
            <tr key={invoice.id} data-testid={`statement-invoice-row-${invoice.id}`}>
              <td data-testid="statement-invoice-id">{invoice.id}</td>
              <td data-testid="statement-invoice-project">{invoice.projectName}</td>
              <td data-testid="statement-invoice-amount">{formatMoney(invoice.amount)}</td>
              <td data-testid="statement-invoice-status">{invoice.status}</td>
              <td data-testid="statement-invoice-due">{formatMoney(invoice.due)}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <p>
        Total owed:{' '}
        <strong data-testid="client-outstanding-total">
          {formatMoney(statement.outstandingTotal)}
        </strong>
      </p>
    </section>
  );
}
