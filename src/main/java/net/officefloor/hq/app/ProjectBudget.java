package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * A project's budget summary. Serialised as JSON by GET /api/projects/budget. {@code budget} is the
 * money agreed for the project (null when none is set); {@code invoiced} is how much has been
 * invoiced against it (the sum of the issued invoices' amounts); {@code remaining} is what is left
 * (budget minus invoiced, null when there is no budget).
 */
public record ProjectBudget(BigDecimal budget, BigDecimal invoiced, BigDecimal remaining) {
}
