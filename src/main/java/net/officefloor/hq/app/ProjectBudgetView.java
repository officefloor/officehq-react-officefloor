package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * A project's budget position: the budget set on it, how much has been invoiced against it (the sum
 * of its invoices), and what is left (budget minus invoiced).
 */
public class ProjectBudgetView {

    private final BigDecimal budget;
    private final BigDecimal invoiced;
    private final BigDecimal remaining;

    public ProjectBudgetView(BigDecimal budget, BigDecimal invoiced) {
        this.budget = budget;
        this.invoiced = invoiced;
        this.remaining = budget.subtract(invoiced);
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
