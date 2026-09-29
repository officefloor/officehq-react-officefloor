package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    // An invoice can carry a percentage discount (discount_pct, V29) taken off its amount; the
    // discounted amount is what the client is actually billed.
    private static final String DISCOUNTED_AMOUNT =
            "(" + AMOUNT_SUM + " * (100 - i.discount_pct) / 100)";

    // The amount still due on the invoice: its discounted amount minus what has been paid, so the
    // discount flows through to what the statement says is owed.
    private static final String DUE = "(" + DISCOUNTED_AMOUNT + " - " + PAID_SUM + ")";

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
        return new ClientStatement(groupByProject(invoices), invoices, outstandingTotal);
    }

    /**
     * Group the (id-ordered) invoices under their job, preserving first-seen job order, and give
     * each job a subtotal — the sum of what is still due across just that job's invoices, so the
     * per-job subtotals add up to the statement's outstanding total.
     */
    private static List<StatementProject> groupByProject(List<StatementInvoice> invoices) {
        Map<Long, List<StatementInvoice>> byProject = new LinkedHashMap<>();
        Map<Long, String> names = new LinkedHashMap<>();
        for (StatementInvoice inv : invoices) {
            byProject.computeIfAbsent(inv.projectId(), k -> new ArrayList<>()).add(inv);
            names.putIfAbsent(inv.projectId(), inv.projectName());
        }
        List<StatementProject> projects = new ArrayList<>();
        for (Map.Entry<Long, List<StatementInvoice>> entry : byProject.entrySet()) {
            BigDecimal subtotal = entry.getValue().stream()
                    .map(StatementInvoice::due)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            projects.add(new StatementProject(entry.getKey(), names.get(entry.getKey()),
                    entry.getValue(), subtotal));
        }
        return projects;
    }
}
