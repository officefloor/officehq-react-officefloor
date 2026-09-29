package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * One row of the cross-project invoice list: an invoice together with the NAME of the project it
 * belongs to (joined) so the UI can show every invoice, its project and its stage in one place.
 * Serialised as JSON by GET /api/invoices/all.
 */
public record InvoiceListing(long id, long projectId, String projectName, BigDecimal amount,
        String status) {
}
