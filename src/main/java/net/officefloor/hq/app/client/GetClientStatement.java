package net.officefloor.hq.app.client;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * GET /api/clients/{id}/statement — one client's statement: every invoice raised for them (across
 * all of their projects) gathered in one place, each with what is still owed on it, plus the total
 * still owed across them all.
 *
 * <p>An invoice's amount is the sum of its line items (qty * unit price) less its discount, with
 * sales tax then added on top of that discounted base; the amount still due is that amount less
 * everything paid against it (the sum of its payments). The
 * outstanding total is the sum of those dues, so a fully paid client shows nothing owed.
 */
public class GetClientStatement {

    public void service(@HttpPathParameter("id") String id, JdbcTemplate jdbc,
            ObjectResponse<ClientStatementView> response) {
        List<StatementInvoiceView> invoices = jdbc.query(
                "SELECT i.id AS id, i.project_id AS project_id, "
                        + "COALESCE((SELECT SUM(li.qty * li.unit_price) FROM line_items li "
                        + "WHERE li.invoice_id = i.id), 0) * (100 - i.discount_pct) / 100 "
                        + "* (100 + i.tax_pct) / 100 "
                        + "- COALESCE((SELECT SUM(p.amount) FROM payments p "
                        + "WHERE p.invoice_id = i.id), 0) AS amount_due "
                        + "FROM invoices i JOIN projects pr ON pr.id = i.project_id "
                        + "WHERE pr.client_id = ? ORDER BY i.id",
                (rs, i) -> new StatementInvoiceView(rs.getLong("id"), rs.getLong("project_id"),
                        rs.getBigDecimal("amount_due")),
                Long.valueOf(id));

        // Group the invoices under their job (project), keeping the query's id order both within a
        // job and across jobs (first-seen), and running a subtotal of what is still due per job.
        Map<Long, List<StatementInvoiceView>> byProject = new LinkedHashMap<>();
        Map<Long, BigDecimal> subtotals = new LinkedHashMap<>();
        BigDecimal outstanding = BigDecimal.ZERO;
        for (StatementInvoiceView invoice : invoices) {
            Long projectId = invoice.getProjectId();
            byProject.computeIfAbsent(projectId, k -> new ArrayList<>()).add(invoice);
            subtotals.merge(projectId, invoice.getAmountDue(), BigDecimal::add);
            outstanding = outstanding.add(invoice.getAmountDue());
        }

        List<StatementProjectView> projects = new ArrayList<>();
        for (Map.Entry<Long, List<StatementInvoiceView>> entry : byProject.entrySet()) {
            projects.add(new StatementProjectView(entry.getKey(), entry.getValue(),
                    subtotals.get(entry.getKey())));
        }

        response.send(new ClientStatementView(invoices, projects, outstanding));
    }
}
