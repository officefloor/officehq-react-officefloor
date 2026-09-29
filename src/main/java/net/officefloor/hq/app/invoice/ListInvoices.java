package net.officefloor.hq.app.invoice;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/** GET /api/invoices?projectId=<id> — every invoice for one project, oldest first. */
public class ListInvoices {

    public void service(@HttpQueryParameter("projectId") String projectId, JdbcTemplate jdbc,
            ObjectResponse<List<InvoiceView>> response) {
        // The invoice amount is the sum of its line items (qty * unit price); an invoice with no
        // lines totals zero. The amount due is that amount less everything paid against it (the sum
        // of its payments), so a fully paid invoice shows zero still to pay.
        List<InvoiceView> invoices = jdbc.query(
                "SELECT id, project_id, status, issued_date, due_date, "
                        + "COALESCE((SELECT SUM(li.qty * li.unit_price) FROM line_items li "
                        + "WHERE li.invoice_id = invoices.id), 0) AS amount, "
                        + "COALESCE((SELECT SUM(li.qty * li.unit_price) FROM line_items li "
                        + "WHERE li.invoice_id = invoices.id), 0) "
                        + "- COALESCE((SELECT SUM(p.amount) FROM payments p "
                        + "WHERE p.invoice_id = invoices.id), 0) AS amount_due FROM invoices"
                        + " WHERE project_id = ? ORDER BY id",
                (rs, i) -> new InvoiceView(rs.getLong("id"), rs.getLong("project_id"),
                        rs.getBigDecimal("amount"), rs.getBigDecimal("amount_due"),
                        rs.getString("status"), rs.getString("issued_date"),
                        rs.getString("due_date")),
                Long.valueOf(projectId));
        response.send(invoices);
    }
}
