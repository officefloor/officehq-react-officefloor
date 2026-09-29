package net.officefloor.hq.app.project;

import java.math.BigDecimal;

/**
 * A project's budget picture: the agreed budget, how much has been invoiced against the project (the
 * sum of every line item on its invoices), and what is left (budget minus invoiced). {@code budget}
 * and {@code remaining} are null when no budget has been set yet.
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
