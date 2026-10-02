import { useEffect, useState } from 'react';
import { formatMoney } from '../../ui/money';

// A statement for a client: all of that client's invoices, grouped under the job (project) they were
// raised against with a subtotal of what is still due per job, plus the total they still owe (the
// sum of what is due across their invoices — equal to the job subtotals added up). Scoped to the
// client via /api/clients/<id>/statement. Opened on demand from the client detail view.
type StatementInvoice = {
  id: number;
  projectId: number;
  projectName: string;
  amount: number;
  status: string;
  due: number;
};
type StatementProject = {
  id: number;
  name: string;
  subtotal: number;
  invoices: StatementInvoice[];
};
type Statement = {
  invoices: StatementInvoice[];
  projects: StatementProject[];
  outstandingTotal: number;
  // The currency the whole statement is shown in — the client's own currency.
  currency: string;
};

export function ClientStatement({ clientId }: { clientId: number }) {
  const [statement, setStatement] = useState<Statement>({
    invoices: [],
    projects: [],
    outstandingTotal: 0,
    currency: 'USD',
  });

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
      {/* A clean, printable summary of the statement: each job with what is still due on it and the
          grand total the client owes, laid out plainly so it reads well on paper. */}
      <section data-testid="statement-print-view" className="statement-print">
        <h2>Statement summary</h2>
        <table data-testid="statement-print-table">
          <thead>
            <tr>
              <th>Job</th>
              <th>Due</th>
            </tr>
          </thead>
          <tbody>
            {statement.projects.map((project) => (
              <tr key={project.id} data-testid={`statement-print-project-${project.id}`}>
                <td data-testid="statement-print-project-name">{project.name}</td>
                <td data-testid="statement-print-project-subtotal">
                  {formatMoney(project.subtotal, statement.currency)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <p className="statement-print-total">
          Grand total owed:{' '}
          <strong data-testid="statement-grand-total">
            {formatMoney(statement.outstandingTotal, statement.currency)}
          </strong>
        </p>
      </section>

      <h2>Statement</h2>
      <table data-testid="client-statement-table">
        <thead>
          <tr>
            <th>Invoice</th>
            <th>Job</th>
            <th>Amount</th>
            <th>Status</th>
            <th>Due</th>
          </tr>
        </thead>
        {statement.projects.map((project) => (
          <tbody key={project.id} data-testid={`statement-project-${project.id}`}>
            <tr data-testid="statement-project-header">
              <th colSpan={4} data-testid="statement-project-name">
                {project.name}
              </th>
              <td data-testid="statement-project-subtotal">{formatMoney(project.subtotal, statement.currency)}</td>
            </tr>
            {project.invoices.map((invoice) => (
              <tr key={invoice.id} data-testid={`statement-invoice-row-${invoice.id}`}>
                <td data-testid="statement-invoice-id">{invoice.id}</td>
                <td data-testid="statement-invoice-project">{invoice.projectName}</td>
                <td data-testid="statement-invoice-amount">{formatMoney(invoice.amount, statement.currency)}</td>
                <td data-testid="statement-invoice-status">{invoice.status}</td>
                <td data-testid="statement-invoice-due">{formatMoney(invoice.due, statement.currency)}</td>
              </tr>
            ))}
          </tbody>
        ))}
      </table>

      <p>
        Total owed:{' '}
        <strong data-testid="client-outstanding-total">
          {formatMoney(statement.outstandingTotal, statement.currency)}
        </strong>
      </p>
    </section>
  );
}
