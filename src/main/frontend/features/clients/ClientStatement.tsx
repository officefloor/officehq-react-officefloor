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

type StatementGroup = {
  projectId: number;
  projectName: string;
  invoices: StatementInvoice[];
  subtotal: number;
};

type Statement = {
  invoices: StatementInvoice[];
  groups: StatementGroup[];
  outstandingTotal: number;
};

function formatAmount(amount: number): string {
  return `$${Number(amount).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
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
       <>
        <table data-testid="client-statement-table">
          <thead>
            <tr>
              <th>Invoice</th>
              <th>Amount</th>
              <th>Due</th>
              <th>Status</th>
            </tr>
          </thead>
          {statement.groups.map((group) => (
            <tbody
              key={group.projectId}
              data-testid={`statement-project-${group.projectId}`}
            >
              <tr>
                <th colSpan={4} data-testid="statement-project-name">
                  {group.projectName}
                </th>
              </tr>
              {group.invoices.map((inv) => (
                <tr key={inv.id} data-testid={`statement-invoice-row-${inv.id}`}>
                  <td data-testid="statement-invoice-id">{inv.id}</td>
                  <td data-testid="statement-invoice-amount">{formatAmount(inv.amount)}</td>
                  <td data-testid="statement-invoice-due">{formatAmount(inv.due)}</td>
                  <td data-testid="statement-invoice-status">{inv.status}</td>
                </tr>
              ))}
              <tr>
                <td>Subtotal</td>
                <td data-testid="statement-project-subtotal" colSpan={3}>
                  {formatAmount(group.subtotal)}
                </td>
              </tr>
            </tbody>
          ))}
          <tfoot>
            <tr>
              <td>Outstanding</td>
              <td data-testid="client-outstanding-total" colSpan={3}>
                {formatAmount(statement.outstandingTotal)}
              </td>
            </tr>
          </tfoot>
        </table>

        {/* A clean, printable summary of the statement: each job's invoices listed plainly with
            what is still owed, closing on the grand total the client owes across everything. This
            is the "print this and hand it over" view; the grand total is the outstanding total
            (what is still due across all of their invoices). */}
        <section data-testid="statement-print-view">
          <h2>Statement summary</h2>
          {statement.groups.map((group) => (
            <div key={group.projectId} data-testid={`statement-print-project-${group.projectId}`}>
              <h3 data-testid="statement-print-project-name">{group.projectName}</h3>
              <ul>
                {group.invoices.map((inv) => (
                  <li key={inv.id} data-testid={`statement-print-invoice-${inv.id}`}>
                    Invoice #{inv.id}: {formatAmount(inv.due)} due
                  </li>
                ))}
              </ul>
            </div>
          ))}
          <p>
            <span>Grand total owed</span>{' '}
            <strong data-testid="statement-grand-total">
              {formatAmount(statement.outstandingTotal)}
            </strong>
          </p>
        </section>
       </>
      ) : null}
    </section>
  );
}
