package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * An invoice billed on a project. Serialised as JSON by the /api/invoices routes; belongs to
 * exactly one project (projectId). Carries a payment status (UNPAID by default, PAID once settled).
 */
public record Invoice(long id, long projectId, BigDecimal amount, String status) {
}
