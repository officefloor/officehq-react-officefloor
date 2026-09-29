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

    public List<Invoice> findByProject(long projectId) {
        return jdbc.query(
                "SELECT id, project_id, amount FROM invoices WHERE project_id = ? ORDER BY id",
                (rs, i) -> new Invoice(rs.getLong("id"), rs.getLong("project_id"),
                        rs.getBigDecimal("amount")),
                projectId);
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
        return new Invoice(keys.getKey().longValue(), projectId, amount);
    }
}
