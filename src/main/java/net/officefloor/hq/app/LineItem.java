package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * A line item on an invoice: one thing being charged for. Serialised as JSON by the
 * /api/lineitems routes; belongs to exactly one invoice (invoiceId). Carries a description, a
 * quantity (qty), the thing that quantity is measured in (unit, e.g. "hours") and the price of each
 * unit (unitPrice). Its contribution to the invoice total is qty * unitPrice.
 */
public record LineItem(long id, long invoiceId, String description, int qty, String unit,
        BigDecimal unitPrice) {
}
