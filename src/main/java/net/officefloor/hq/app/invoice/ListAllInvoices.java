package net.officefloor.hq.app.invoice;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * GET /api/all-invoices — every invoice across all projects, each with its project name and stage.
 * An optional {@code status} query parameter narrows the list to a single stage; {@code ALL} (or a
 * blank value) leaves the list unfiltered.
 */
public class ListAllInvoices {

    public void service(@HttpQueryParameter("status") String status, JdbcTemplate jdbc,
            ObjectResponse<List<AllInvoiceView>> response) {
        RowMapper<AllInvoiceView> mapper = (rs, i) -> new AllInvoiceView(rs.getLong("id"),
                rs.getLong("project_id"), rs.getString("project_name"), rs.getBigDecimal("amount"),
                rs.getString("status"));
        boolean all = status == null || status.isBlank() || "ALL".equalsIgnoreCase(status);
        String base = "SELECT i.id, i.project_id, p.name AS project_name, i.status, "
                + "COALESCE((SELECT SUM(li.qty * li.unit_price) FROM line_items li "
                + "WHERE li.invoice_id = i.id), 0) AS amount "
                + "FROM invoices i JOIN projects p ON p.id = i.project_id ";
        List<AllInvoiceView> invoices = all
                ? jdbc.query(base + "ORDER BY i.id", mapper)
                : jdbc.query(base + "WHERE i.status = ? ORDER BY i.id", mapper, status);
        response.send(invoices);
    }
}
