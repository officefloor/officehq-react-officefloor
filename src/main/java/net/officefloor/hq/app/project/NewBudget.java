package net.officefloor.hq.app.project;

import java.math.BigDecimal;

/** Request body for setting a project's budget. */
public class NewBudget {

    private BigDecimal budget;

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }
}
