import React, { useState } from 'react';

// A client's statement, shown on demand when a client is opened from the clients list: all of that
// client's invoices in one place with the total they still owe. Owns its own state (CLAUDE.md —
// features own their state; clients does not import the invoices feature). Reads from the dedicated
// GET /api/clients/statement endpoint, which derives each invoice's amount and amount still due.
type StatementInvoice = {
  id: number;
  projectId: number;
  projectName: string;
  amount: number;
  due: number;
  status: string;
};

type Statement = { invoices: StatementInvoice[]; outstandingTotal: number };

function money(n: number): string {
  return `$${Number(n).toFixed(2)}`;
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
        <>
          <table data-testid="client-statement-table">
            <thead>
              <tr>
                <th>Project</th>
                <th>Amount</th>
                <th>Due</th>
                <th>Stage</th>
              </tr>
            </thead>
            <tbody>
              {statement.invoices.map((inv) => (
                <tr key={inv.id} data-testid={`statement-invoice-row-${inv.id}`}>
                  <td data-testid="statement-invoice-project">{inv.projectName}</td>
                  <td data-testid="statement-invoice-amount">{money(inv.amount)}</td>
                  <td data-testid="statement-invoice-due">{money(inv.due)}</td>
                  <td data-testid="statement-invoice-status">{inv.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <p>
            Total owed:{' '}
            <span data-testid="client-outstanding-total">{money(statement.outstandingTotal)}</span>
          </p>
        </>
      ) : null}
    </section>
  );
}
