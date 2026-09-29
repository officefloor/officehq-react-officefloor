import React, { useState } from 'react';

// A client's statement, shown on demand when a client is opened from the clients list: the client's
// invoices grouped by job, each job showing a subtotal, plus the total they still owe across every
// job. Owns its own state (CLAUDE.md — features own their state; clients does not import the invoices
// feature). Reads from the dedicated GET /api/clients/statement endpoint, which derives each
// invoice's amount and amount still due and groups them by job with per-job subtotals.
type StatementInvoice = {
  id: number;
  projectId: number;
  projectName: string;
  amount: number;
  due: number;
  status: string;
};

type StatementProject = {
  projectId: number;
  projectName: string;
  invoices: StatementInvoice[];
  subtotal: number;
};

type Statement = {
  projects: StatementProject[];
  invoices: StatementInvoice[];
  outstandingTotal: number;
};

function money(n: number): string {
  return Number(n).toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}

export function ClientStatement({ clientId }: { clientId: number }) {
  const [statement, setStatement] = useState<Statement | null>(null);

  async function open() {
    const res = await fetch(`/api/clients/statement?clientId=${clientId}`);
    if (res.ok) {
      setStatement(await res.json());
    }
  }

  return (
    <section data-testid="client-statement">
      <button type="button" data-testid="client-statement-open" onClick={open}>
        Statement
      </button>
      {statement ? (
        <article data-testid="statement-print-view" className="statement-print-view">
          <header>
            <h3>Statement of account</h3>
          </header>
          {statement.projects.map((project) => (
            <div
              key={project.projectId}
              data-testid={`statement-project-${project.projectId}`}
            >
              <h4 data-testid="statement-project-name">{project.projectName}</h4>
              <table data-testid="client-statement-table">
                <thead>
                  <tr>
                    <th>Job</th>
                    <th>Amount</th>
                    <th>Due</th>
                    <th>Stage</th>
                  </tr>
                </thead>
                <tbody>
                  {project.invoices.map((inv) => (
                    <tr key={inv.id} data-testid={`statement-invoice-row-${inv.id}`}>
                      <td data-testid="statement-invoice-project">{inv.projectName}</td>
                      <td data-testid="statement-invoice-amount">{money(inv.amount)}</td>
                      <td data-testid="statement-invoice-due">{money(inv.due)}</td>
                      <td data-testid="statement-invoice-status">{inv.status}</td>
                    </tr>
                  ))}
                </tbody>
                <tfoot>
                  <tr>
                    <td>Subtotal</td>
                    <td colSpan={3} data-testid="statement-project-subtotal">
                      {money(project.subtotal)}
                    </td>
                  </tr>
                </tfoot>
              </table>
            </div>
          ))}
          <p>
            Total owed:{' '}
            <span data-testid="client-outstanding-total">{money(statement.outstandingTotal)}</span>
          </p>
          <p className="statement-grand-total-line">
            Grand total owed:{' '}
            <strong data-testid="statement-grand-total">{money(statement.outstandingTotal)}</strong>
          </p>
        </article>
      ) : null}
    </section>
  );
}
