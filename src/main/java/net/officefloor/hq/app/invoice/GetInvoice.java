package net.officefloor.hq.app.invoice;

import java.math.BigDecimal;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * GET /api/invoices/{id} — one invoice, with its amount (sum of line items), the amount still due
 * (that less everything paid), and its status worked out from the payments recorded against it.
 */
public class GetInvoice {

    public void service(@HttpPathParameter("id") String id, JdbcTemplate jdbc,
            ObjectResponse<InvoiceView> response) {
        // The amount is the subtotal (sum of line items) less its discount, then with sales tax
        // added on top of that discounted base — tax is worked out after the discount. The amount
        // due is that same taxed amount less everything paid against it.
        InvoiceView invoice = jdbc.query(
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
                        + "FROM invoices WHERE id = ?",
                (rs, i) -> {
                    BigDecimal amount = rs.getBigDecimal("amount");
                    BigDecimal amountDue = rs.getBigDecimal("amount_due");
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
                Long.valueOf(id)).stream().findFirst()
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "No such invoice"));
        response.send(invoice);
    }
}
