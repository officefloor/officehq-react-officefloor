package net.officefloor.hq.app.invoice;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/** GET /api/invoices?projectId=<id> — every invoice for one project, oldest first. */
public class ListInvoices {

    public void service(@HttpQueryParameter("projectId") String projectId, JdbcTemplate jdbc,
            ObjectResponse<List<InvoiceView>> response) {
        // The invoice amount is the sum of its line items (qty * unit price) less its discount, then
        // with sales tax added on top of that discounted base (tax is worked out after the
        // discount); an invoice with no lines totals zero. The amount due is that amount less
        // everything paid against it (the sum of its payments), so a fully paid invoice shows zero
        // still to pay.
        List<InvoiceView> invoices = jdbc.query(
                "SELECT id, project_id, status, issued_date, due_date, discount_pct, tax_pct, "
                        + "COALESCE((SELECT SUM(li.qty * li.unit_price) FROM line_items li "
                        + "WHERE li.invoice_id = invoices.id), 0) * (100 - discount_pct) / 100 "
                        + "* (100 + tax_pct) / 100 AS amount, "
                        + "COALESCE((SELECT SUM(li.qty * li.unit_price) FROM line_items li "
                        + "WHERE li.invoice_id = invoices.id), 0) * (100 - discount_pct) / 100 "
                        + "* (100 + tax_pct) / 100 "
                        + "- COALESCE((SELECT SUM(p.amount) FROM payments p "
                        + "WHERE p.invoice_id = invoices.id), 0) AS amount_due, "
                        + "(SELECT c.currency FROM clients c JOIN projects pr "
                        + "ON pr.client_id = c.id WHERE pr.id = invoices.project_id) AS currency "
                        + "FROM invoices WHERE project_id = ? ORDER BY id",
                (rs, i) -> {
                    java.math.BigDecimal amount = rs.getBigDecimal("amount");
                    java.math.BigDecimal amountDue = rs.getBigDecimal("amount_due");
                    // Paid = the whole amount less what is still due; the status is worked out from
                    // that rather than read from the stored flag.
                    String status = InvoiceStatus.derive(rs.getString("status"), amount,
                            amount.subtract(amountDue));
                    InvoiceView view = new InvoiceView(rs.getLong("id"), rs.getLong("project_id"),
                            amount, amountDue, status, rs.getString("issued_date"),
                            rs.getString("due_date"));
                    view.setDiscountPct(rs.getBigDecimal("discount_pct"));
                    view.setTaxPct(rs.getBigDecimal("tax_pct"));
                    view.setCurrency(rs.getString("currency"));
                    return view;
                },
                Long.valueOf(projectId));
        response.send(invoices);
    }
}
