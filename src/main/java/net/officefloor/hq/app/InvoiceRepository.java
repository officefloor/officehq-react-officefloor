package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** Data access for {@link Invoice} rows (JdbcTemplate over the schema in V4__invoices.sql). */
@Repository
public class InvoiceRepository {

    private final JdbcTemplate jdbc;

    public InvoiceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final org.springframework.jdbc.core.RowMapper<Invoice> MAPPER =
            (rs, i) -> new Invoice(rs.getLong("id"), rs.getLong("project_id"),
                    rs.getBigDecimal("amount"), rs.getString("status"),
                    rs.getString("issued_date"), rs.getString("due_date"));

    // An invoice's amount is the sum of its line items (qty * unit_price); a line-item-less invoice
    // totals zero. Derived here so the amount always reflects the current line items.
    private static final String AMOUNT_SUM =
            "COALESCE((SELECT SUM(li.qty * li.unit_price) FROM line_items li"
                    + " WHERE li.invoice_id = i.id), 0)";

    public List<Invoice> findByProject(long projectId) {
        return jdbc.query(
                "SELECT i.id, i.project_id, " + AMOUNT_SUM + " AS amount, i.status, i.issued_date,"
                        + " i.due_date FROM invoices i WHERE i.project_id = ? ORDER BY i.id",
                MAPPER, projectId);
    }

    /** A project's invoices ordered by due date, earliest first (nulls last, id as tiebreak). */
    public List<Invoice> findByProjectOrderByDueDate(long projectId) {
        return jdbc.query(
                "SELECT i.id, i.project_id, " + AMOUNT_SUM + " AS amount, i.status, i.issued_date,"
                        + " i.due_date FROM invoices i WHERE i.project_id = ?"
                        + " ORDER BY i.due_date ASC NULLS LAST, i.id",
                MAPPER, projectId);
    }

    /** Every invoice across all projects, joined to its project name, ordered by id. */
    public List<InvoiceListing> findAllWithProject() {
        return jdbc.query(
                "SELECT i.id, i.project_id, p.name AS project_name, " + AMOUNT_SUM + " AS amount,"
                        + " i.status FROM invoices i JOIN projects p ON p.id = i.project_id"
                        + " ORDER BY i.id",
                (rs, i) -> new InvoiceListing(rs.getLong("id"), rs.getLong("project_id"),
                        rs.getString("project_name"), rs.getBigDecimal("amount"),
                        rs.getString("status")));
    }

    public Invoice findById(long id) {
        List<Invoice> found = jdbc.query(
                "SELECT i.id, i.project_id, " + AMOUNT_SUM + " AS amount, i.status, i.issued_date,"
                        + " i.due_date FROM invoices i WHERE i.id = ?", MAPPER, id);
        return found.isEmpty() ? null : found.get(0);
    }

    public Invoice create(long projectId, BigDecimal amount) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO invoices (project_id, amount) VALUES (?, ?)", new String[] {"id"});
            ps.setLong(1, projectId);
            ps.setBigDecimal(2, amount);
            return ps;
        }, keys);
        return new Invoice(keys.getKey().longValue(), projectId, amount, "DRAFT", null, null);
    }

    /** Flip an invoice from DRAFT to SENT and return the updated row (null if no such invoice). */
    public Invoice markSent(long id) {
        jdbc.update("UPDATE invoices SET status = 'SENT' WHERE id = ?", id);
        return findById(id);
    }

    /** Flip an invoice to PAID and return the updated row (null if no such invoice). */
    public Invoice markPaid(long id) {
        jdbc.update("UPDATE invoices SET status = 'PAID' WHERE id = ?", id);
        return findById(id);
    }
}
