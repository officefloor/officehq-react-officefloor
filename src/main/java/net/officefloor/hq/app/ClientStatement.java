package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.util.List;

/**
 * A client's statement: all of that client's invoices, both grouped by job under {@code projects}
 * (each job carrying its own subtotal) and kept as one flat {@code invoices} list, together with the
 * {@code outstandingTotal} — the sum of what is still due across every invoice. Serialised as JSON
 * by GET /api/clients/statement?clientId=&lt;id&gt;.
 */
public record ClientStatement(List<StatementProject> projects, List<StatementInvoice> invoices,
        BigDecimal outstandingTotal, String currency) {
}
