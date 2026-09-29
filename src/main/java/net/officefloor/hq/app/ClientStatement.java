package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.util.List;

/**
 * A client's statement: all of that client's invoices in one place, together with the
 * {@code outstandingTotal} — the sum of what is still due across those invoices. Serialised as JSON
 * by GET /api/clients/statement?clientId=&lt;id&gt;.
 */
public record ClientStatement(List<StatementInvoice> invoices, BigDecimal outstandingTotal) {
}
