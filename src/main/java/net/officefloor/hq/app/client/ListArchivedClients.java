package net.officefloor.hq.app.client;

import java.util.List;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * GET /api/clients/archived — the clients that were tucked away (archived), each with how much they
 * still owe, so the "show archived" view can list them for restoring. The mirror of {@link
 * ListClients}, which returns only the clients still in play; the outstanding calculation is the
 * same, just filtered to the archived rows.
 */
public class ListArchivedClients {

    public void service(JdbcTemplate jdbc, ObjectResponse<List<ClientListView>> response) {
        List<ClientListView> clients = jdbc.query(
                "SELECT c.id AS id, c.name AS name, c.email AS email, "
                        + "COALESCE((SELECT SUM("
                        + "COALESCE((SELECT SUM(li.qty * li.unit_price) FROM line_items li "
                        + "WHERE li.invoice_id = i.id), 0) * (100 - i.discount_pct) / 100 "
                        + "* (100 + i.tax_pct) / 100 "
                        + "- COALESCE((SELECT SUM(p.amount) FROM payments p "
                        + "WHERE p.invoice_id = i.id), 0)) "
                        + "FROM invoices i JOIN projects pr ON pr.id = i.project_id "
                        + "WHERE pr.client_id = c.id), 0) AS outstanding "
                        + "FROM clients c WHERE c.archived = TRUE ORDER BY c.id",
                (rs, i) -> new ClientListView(rs.getLong("id"), rs.getString("name"),
                        rs.getString("email"), rs.getBigDecimal("outstanding")));
        response.send(clients);
    }
}
