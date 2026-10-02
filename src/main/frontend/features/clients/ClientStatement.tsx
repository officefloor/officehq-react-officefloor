import React, { useEffect, useState } from 'react';

// A client's statement, rendered inside the clients feature when a client is opened: all of that
// client's invoices (gathered from every one of their projects) in one place, each with how much is
// still left to pay, and the outstanding total — what they still owe across those invoices. Opens on
// demand (client-statement-open) and then reads /api/clients/<id>/statement. Owns its own state and
// data loading (no global store); composed, not branched.
type StatementInvoice = {
  id: number;
  projectId: number;
  amount: number;
  due: number;
  status: string;
};

type Statement = { invoices: StatementInvoice[]; outstandingTotal: number };

function formatAmount(amount: number): string {
  return `$${Number(amount).toFixed(2)}`;
}

export function ClientStatement({ clientId }: { clientId: number }) {
  const [statement, setStatement] = useState<Statement | null>(null);

  // Collapse the statement when the opened client changes, so it is opened fresh per client.
  useEffect(() => {
    setStatement(null);
  }, [clientId]);

  async function open() {
    const res = await fetch(`/api/clients/${clientId}/statement`);
    if (!res.ok) {
      return;
    }
    setStatement(await res.json());
  }

  return (
    <section data-testid="client-statement">
      <button type="button" data-testid="client-statement-open" onClick={() => void open()}>
        Statement
      </button>

      {statement ? (
        <table data-testid="client-statement-table">
          <thead>
            <tr>
              <th>Invoice</th>
              <th>Amount</th>
              <th>Due</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {statement.invoices.map((inv) => (
              <tr key={inv.id} data-testid={`statement-invoice-row-${inv.id}`}>
                <td data-testid="statement-invoice-id">{inv.id}</td>
                <td data-testid="statement-invoice-amount">{formatAmount(inv.amount)}</td>
                <td data-testid="statement-invoice-due">{formatAmount(inv.due)}</td>
                <td data-testid="statement-invoice-status">{inv.status}</td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr>
              <td>Outstanding</td>
              <td data-testid="client-outstanding-total" colSpan={3}>
                {formatAmount(statement.outstandingTotal)}
              </td>
            </tr>
          </tfoot>
        </table>
      ) : null}
    </section>
  );
}
