package net.officefloor.hq.app.invoice;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/** GET /api/invoices/{id}/payments — the payments a client has made against one invoice, oldest first. */
public class ListInvoicePayments {

    public void service(@HttpPathParameter("id") String id, JdbcTemplate jdbc,
            ObjectResponse<List<PaymentView>> response) {
        List<PaymentView> payments = jdbc.query(
                "SELECT id, invoice_id, amount, paid_date FROM payments "
                        + "WHERE invoice_id = ? ORDER BY id",
                (rs, i) -> new PaymentView(rs.getLong("id"), rs.getLong("invoice_id"),
                        rs.getBigDecimal("amount"), rs.getString("paid_date")),
                Long.valueOf(id));
        response.send(payments);
    }
}
