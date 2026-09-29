package net.officefloor.hq.app.invoice;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/** GET /api/invoices?projectId=<id> — every invoice for one project, oldest first. */
public class ListInvoices {

    public void service(@HttpQueryParameter("projectId") String projectId, JdbcTemplate jdbc,
            ObjectResponse<List<InvoiceView>> response) {
        List<InvoiceView> invoices = jdbc.query(
                "SELECT id, project_id, amount, status FROM invoices WHERE project_id = ? ORDER BY id",
                (rs, i) -> new InvoiceView(rs.getLong("id"), rs.getLong("project_id"),
                        rs.getBigDecimal("amount"), rs.getString("status")),
                Long.valueOf(projectId));
        response.send(invoices);
    }
}
