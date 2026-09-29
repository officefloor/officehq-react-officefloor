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

    public List<Invoice> findByProject(long projectId) {
        return jdbc.query(
                "SELECT id, project_id, amount, status, issued_date, due_date FROM invoices"
                        + " WHERE project_id = ? ORDER BY id",
                MAPPER, projectId);
    }

    /** A project's invoices ordered by due date, earliest first (nulls last, id as tiebreak). */
    public List<Invoice> findByProjectOrderByDueDate(long projectId) {
        return jdbc.query(
                "SELECT id, project_id, amount, status, issued_date, due_date FROM invoices"
                        + " WHERE project_id = ? ORDER BY due_date ASC NULLS LAST, id",
                MAPPER, projectId);
    }

    public Invoice findById(long id) {
        List<Invoice> found = jdbc.query(
                "SELECT id, project_id, amount, status, issued_date, due_date FROM invoices"
                        + " WHERE id = ?", MAPPER, id);
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
        return new Invoice(keys.getKey().longValue(), projectId, amount, "UNPAID", null, null);
    }

    /** Flip an invoice to PAID and return the updated row (null if no such invoice). */
    public Invoice markPaid(long id) {
        jdbc.update("UPDATE invoices SET status = 'PAID' WHERE id = ?", id);
        return findById(id);
    }
}
