package net.officefloor.hq.app.invoice;

import java.util.List;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/** GET /api/all-invoices — every invoice across all projects, each with its project name and stage. */
public class ListAllInvoices {

    public void service(JdbcTemplate jdbc, ObjectResponse<List<AllInvoiceView>> response) {
        List<AllInvoiceView> invoices = jdbc.query(
                "SELECT i.id, i.project_id, p.name AS project_name, i.amount, i.status "
                        + "FROM invoices i JOIN projects p ON p.id = i.project_id "
                        + "ORDER BY i.id",
                (rs, i) -> new AllInvoiceView(rs.getLong("id"), rs.getLong("project_id"),
                        rs.getString("project_name"), rs.getBigDecimal("amount"),
                        rs.getString("status")));
        response.send(invoices);
    }
}
