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
        InvoiceView invoice = jdbc.query(
                "SELECT id, project_id, status, issued_date, due_date, "
                        + "COALESCE((SELECT SUM(li.qty * li.unit_price) FROM line_items li "
                        + "WHERE li.invoice_id = invoices.id), 0) AS amount, "
                        + "COALESCE((SELECT SUM(li.qty * li.unit_price) FROM line_items li "
                        + "WHERE li.invoice_id = invoices.id), 0) "
                        + "- COALESCE((SELECT SUM(p.amount) FROM payments p "
                        + "WHERE p.invoice_id = invoices.id), 0) AS amount_due FROM invoices"
                        + " WHERE id = ?",
                (rs, i) -> {
                    BigDecimal amount = rs.getBigDecimal("amount");
                    BigDecimal amountDue = rs.getBigDecimal("amount_due");
                    String status = InvoiceStatus.derive(rs.getString("status"), amount,
                            amount.subtract(amountDue));
                    return new InvoiceView(rs.getLong("id"), rs.getLong("project_id"), amount,
                            amountDue, status, rs.getString("issued_date"),
                            rs.getString("due_date"));
                },
                Long.valueOf(id)).stream().findFirst()
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "No such invoice"));
        response.send(invoice);
    }
}
