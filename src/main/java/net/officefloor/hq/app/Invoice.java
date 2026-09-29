package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * An invoice billed on a project. Serialised as JSON by the /api/invoices routes; belongs to
 * exactly one project (projectId). Carries a payment status (UNPAID by default, PAID once settled)
 * and two dates: issuedDate (when it went out) and dueDate (when it is due), as ISO yyyy-MM-dd.
 * {@code due} is how much is still owed: the invoice amount minus everything paid against it.
 * {@code discountPct} is a percentage (0..100) taken off the subtotal; {@code taxPct} is a sales
 * tax percentage (0..100) added on top after the discount. The front-end shows the subtotal, the
 * discount, the tax and the final total ((subtotal minus the discount) times (1 + taxPct/100)).
 */
public record Invoice(long id, long projectId, BigDecimal amount, String status, String issuedDate,
        String dueDate, BigDecimal due, BigDecimal discountPct, BigDecimal taxPct) {
}
