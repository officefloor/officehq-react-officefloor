package net.officefloor.hq.app.projects;

import java.math.BigDecimal;

/**
 * JSON response shape for a project's budget position: the budget set on the project, how much has
 * been invoiced against it (the sum of its invoice amounts), and what is left (budget minus invoiced).
 */
public class ProjectBudgetView {

    private final BigDecimal budget;
    private final BigDecimal invoiced;
    private final BigDecimal remaining;

    public ProjectBudgetView(BigDecimal budget, BigDecimal invoiced, BigDecimal remaining) {
        this.budget = budget;
        this.invoiced = invoiced;
        this.remaining = remaining;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public BigDecimal getInvoiced() {
        return invoiced;
    }

    public BigDecimal getRemaining() {
        return remaining;
    }
}
