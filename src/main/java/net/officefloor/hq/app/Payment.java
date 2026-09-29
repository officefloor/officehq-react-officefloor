package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * A payment a client has made against an invoice. Serialised as JSON by the /api/payments routes;
 * belongs to exactly one invoice (invoiceId). Carries how much was paid (amount) and when (date, as
 * ISO yyyy-MM-dd).
 */
public record Payment(long id, long invoiceId, BigDecimal amount, String date) {
}
