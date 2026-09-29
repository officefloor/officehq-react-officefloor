import React, { useEffect, useState } from 'react';

// A project's budget summary, shown when a project is opened from the projects list. Owns its own
// state (CLAUDE.md — features own their state). Shows the agreed budget, how much has been invoiced
// against it, and what is left (budget minus invoiced). Amounts render as US dollars with grouping.
type Budget = {
  budget: number | null;
  invoiced: number;
  remaining: number | null;
};

function money(n: number): string {
  return n.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}

export function ProjectBudget({ projectId }: { projectId: number }) {
  const [budget, setBudget] = useState<Budget | null>(null);

  useEffect(() => {
    let live = true;
    void (async () => {
      const res = await fetch(`/api/projects/budget?projectId=${projectId}`);
      if (res.ok && live) {
        setBudget(await res.json());
      }
    })();
    return () => {
      live = false;
    };
  }, [projectId]);

  if (budget === null) {
    return null;
  }

  return (
    <section data-testid="project-budget-summary">
      <h2>Budget</h2>
      <dl>
        <dt>Budget</dt>
        <dd data-testid="project-budget">{budget.budget === null ? '' : money(budget.budget)}</dd>
        <dt>Invoiced</dt>
        <dd data-testid="project-invoiced">{money(budget.invoiced)}</dd>
        <dt>Remaining</dt>
        <dd data-testid="project-remaining">
          {budget.remaining === null ? '' : money(budget.remaining)}
        </dd>
      </dl>
    </section>
  );
}
