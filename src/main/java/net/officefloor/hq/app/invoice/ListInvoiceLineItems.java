package net.officefloor.hq.app.invoice;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/** GET /api/invoices/{id}/line-items — the things one invoice is charging for, oldest first. */
public class ListInvoiceLineItems {

    public void service(@HttpPathParameter("id") String id, JdbcTemplate jdbc,
            ObjectResponse<List<LineItemView>> response) {
        List<LineItemView> items = jdbc.query(
                "SELECT id, invoice_id, description, qty, unit_price FROM line_items "
                        + "WHERE invoice_id = ? ORDER BY id",
                (rs, i) -> new LineItemView(rs.getLong("id"), rs.getLong("invoice_id"),
                        rs.getString("description"), rs.getInt("qty"),
                        rs.getBigDecimal("unit_price")),
                Long.valueOf(id));
        response.send(items);
    }
}
