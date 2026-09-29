package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Read-only query backing a client's statement: every invoice belonging to the client (across all of
 * their projects), each with its derived amount, amount still due and status, plus the outstanding
 * total (the sum of what is still due). Amount, paid and due are derived the same way as
 * {@link InvoiceRepository} so a statement reads consistently with the rest of the app.
 */
@Repository
public class ClientStatementRepository {

    private final JdbcTemplate jdbc;

    public ClientStatementRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // An invoice's amount is the sum of its line items (qty * unit_price); zero when it has none.
    private static final String AMOUNT_SUM =
            "COALESCE((SELECT SUM(li.qty * li.unit_price) FROM line_items li"
                    + " WHERE li.invoice_id = i.id), 0)";

    // What has been paid against the invoice: the sum of its payments (zero when none).
    private static final String PAID_SUM =
            "COALESCE((SELECT SUM(pm.amount) FROM payments pm"
                    + " WHERE pm.invoice_id = i.id), 0)";

    // The amount still due on the invoice: its amount minus what has been paid.
    private static final String DUE = "(" + AMOUNT_SUM + " - " + PAID_SUM + ")";

    // Status worked out from payments: DRAFT until issued, SENT once issued but unpaid, PARTIAL once
    // some (but not all) is paid, PAID once the payments cover the amount.
    private static final String DERIVED_STATUS =
            "CASE WHEN i.status = 'DRAFT' THEN 'DRAFT'"
                    + " WHEN " + AMOUNT_SUM + " > 0 AND " + PAID_SUM + " >= " + AMOUNT_SUM
                    + " THEN 'PAID'"
                    + " WHEN " + PAID_SUM + " > 0 THEN 'PARTIAL'"
                    + " ELSE 'SENT' END";

    public ClientStatement forClient(long clientId) {
        List<StatementInvoice> invoices = jdbc.query(
                "SELECT i.id, i.project_id, p.name AS project_name, " + AMOUNT_SUM + " AS amount,"
                        + " " + DUE + " AS due, " + DERIVED_STATUS + " AS status"
                        + " FROM invoices i JOIN projects p ON p.id = i.project_id"
                        + " WHERE p.client_id = ? ORDER BY i.id",
                (rs, i) -> new StatementInvoice(rs.getLong("id"), rs.getLong("project_id"),
                        rs.getString("project_name"), rs.getBigDecimal("amount"),
                        rs.getBigDecimal("due"), rs.getString("status")),
                clientId);
        BigDecimal outstandingTotal = invoices.stream()
                .map(StatementInvoice::due)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ClientStatement(invoices, outstandingTotal);
    }
}
