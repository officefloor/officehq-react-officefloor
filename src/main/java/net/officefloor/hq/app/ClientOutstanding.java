package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * How much one client still owes: the sum of what is due across all of their invoices. Serialised as
 * JSON by GET /api/clients/outstanding so the clients list can be sorted by amount owed.
 */
public record ClientOutstanding(long clientId, BigDecimal outstanding) {
}
