import React, { useEffect, useState } from 'react';

// A project's budget position, shown inside the projects feature when a project is opened: the budget
// set on it, how much has been invoiced against it, and what is left (budget minus invoiced). Owns
// its own state and data loading (no global store).
type Budget = {
  budget: number;
  invoiced: number;
  remaining: number;
};

// Money the owner reads, e.g. $1,000.00 — grouped thousands, always two decimals.
const money = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
});

export function ProjectBudget({ projectId }: { projectId: number }) {
  const [budget, setBudget] = useState<Budget | null>(null);

  useEffect(() => {
    async function load() {
      const res = await fetch(`/api/projects/${projectId}/budget`);
      if (!res.ok) {
        return;
      }
      setBudget(await res.json());
    }
    void load();
  }, [projectId]);

  if (budget === null) {
    return null;
  }

  return (
    <section data-testid="project-budget-section">
      <h2>Budget</h2>
      <dl>
        <dt>Budget</dt>
        <dd data-testid="project-budget">{money.format(Number(budget.budget))}</dd>
        <dt>Invoiced</dt>
        <dd data-testid="project-invoiced">{money.format(Number(budget.invoiced))}</dd>
        <dt>Remaining</dt>
        <dd data-testid="project-remaining">{money.format(Number(budget.remaining))}</dd>
      </dl>
    </section>
  );
}
