package net.officefloor.hq.app.client;

import java.util.List;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * GET /api/clients — every client still in play (archived clients are tucked away), each with how
 * much they still owe so the list can be sorted by name or by outstanding amount.
 *
 * <p>A client's outstanding is the sum, across every invoice raised for them (via their projects),
 * of what is still due on that invoice — line items (qty * unit price) less its discount, plus sales
 * tax, less everything paid against it. This mirrors the per-client statement total.
 */
public class ListClients {

    public void service(JdbcTemplate jdbc, ObjectResponse<List<ClientListView>> response) {
        List<ClientListView> clients = jdbc.query(
                "SELECT c.id AS id, c.name AS name, c.email AS email, c.currency AS currency, "
                        + "COALESCE((SELECT SUM("
                        + "COALESCE((SELECT SUM(li.qty * li.unit_price) FROM line_items li "
                        + "WHERE li.invoice_id = i.id), 0) * (100 - i.discount_pct) / 100 "
                        + "* (100 + i.tax_pct) / 100 "
                        + "- COALESCE((SELECT SUM(p.amount) FROM payments p "
                        + "WHERE p.invoice_id = i.id), 0)) "
                        + "FROM invoices i JOIN projects pr ON pr.id = i.project_id "
                        + "WHERE pr.client_id = c.id), 0) AS outstanding "
                        + "FROM clients c WHERE c.archived = FALSE ORDER BY c.id",
                (rs, i) -> new ClientListView(rs.getLong("id"), rs.getString("name"),
                        rs.getString("email"), rs.getString("currency"),
                        rs.getBigDecimal("outstanding")));
        response.send(clients);
    }
}
