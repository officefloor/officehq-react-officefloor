package net.officefloor.hq.app.dashboard;

import java.util.List;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * GET /api/dashboard/top-clients — the home screen's top five clients ranked by how much they still
 * owe, most owed first.
 *
 * <p>A client's owed amount is the same "outstanding" figure the clients list and statement use: the
 * sum, across every invoice raised for them (via their projects), of that invoice's total — line
 * items (qty * unit price) less its discount, plus sales tax — less everything paid against it.
 * Archived clients are tucked away, and only clients that actually owe something appear.
 */
public class GetTopClients {

    public void service(JdbcTemplate jdbc, ObjectResponse<List<TopClientView>> response) {
        List<TopClientView> top = jdbc.query(
                "SELECT id, name, outstanding FROM ("
                        + "SELECT c.id AS id, c.name AS name, "
                        + "COALESCE((SELECT SUM("
                        + "COALESCE((SELECT SUM(li.qty * li.unit_price) FROM line_items li "
                        + "WHERE li.invoice_id = i.id), 0) * (100 - i.discount_pct) / 100 "
                        + "* (100 + i.tax_pct) / 100 "
                        + "- COALESCE((SELECT SUM(p.amount) FROM payments p "
                        + "WHERE p.invoice_id = i.id), 0)) "
                        + "FROM invoices i JOIN projects pr ON pr.id = i.project_id "
                        + "WHERE pr.client_id = c.id), 0) AS outstanding "
                        + "FROM clients c WHERE c.archived = FALSE) t "
                        + "WHERE outstanding > 0 "
                        + "ORDER BY outstanding DESC, name ASC LIMIT 5",
                (rs, i) -> new TopClientView(rs.getLong("id"), rs.getString("name"),
                        rs.getBigDecimal("outstanding")));
        response.send(top);
    }
}
