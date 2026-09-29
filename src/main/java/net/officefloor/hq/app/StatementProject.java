package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.util.List;

/**
 * One job (project) on a client's statement: the client's invoices raised against that project,
 * grouped together, with the {@code subtotal} — the sum of what is still due across just those
 * invoices. The per-job subtotals add up to the statement's {@code outstandingTotal}. Serialised as
 * JSON by GET /api/clients/statement.
 */
public record StatementProject(long projectId, String projectName, List<StatementInvoice> invoices,
        BigDecimal subtotal) {
}
