package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** Data access for {@link LineItem} rows (JdbcTemplate over the schema in V12__line_items.sql). */
@Repository
public class LineItemRepository {

    private final JdbcTemplate jdbc;

    public LineItemRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final org.springframework.jdbc.core.RowMapper<LineItem> MAPPER =
            (rs, i) -> new LineItem(rs.getLong("id"), rs.getLong("invoice_id"),
                    rs.getString("description"), rs.getInt("qty"), rs.getBigDecimal("unit_price"));

    public List<LineItem> findByInvoice(long invoiceId) {
        return jdbc.query(
                "SELECT id, invoice_id, description, qty, unit_price FROM line_items"
                        + " WHERE invoice_id = ? ORDER BY id",
                MAPPER, invoiceId);
    }

    public LineItem create(long invoiceId, String description, int qty, BigDecimal unitPrice) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO line_items (invoice_id, description, qty, unit_price)"
                            + " VALUES (?, ?, ?, ?)",
                    new String[] {"id"});
            ps.setLong(1, invoiceId);
            ps.setString(2, description);
            ps.setInt(3, qty);
            ps.setBigDecimal(4, unitPrice);
            return ps;
        }, keys);
        return new LineItem(keys.getKey().longValue(), invoiceId, description, qty, unitPrice);
    }

    public LineItem findById(long id) {
        List<LineItem> found = jdbc.query(
                "SELECT id, invoice_id, description, qty, unit_price FROM line_items WHERE id = ?",
                MAPPER, id);
        return found.isEmpty() ? null : found.get(0);
    }

    public LineItem update(long id, String description, int qty, BigDecimal unitPrice) {
        LineItem existing = findById(id);
        if (existing == null) {
            return null;
        }
        jdbc.update(
                "UPDATE line_items SET description = ?, qty = ?, unit_price = ? WHERE id = ?",
                description, qty, unitPrice, id);
        return new LineItem(id, existing.invoiceId(), description, qty, unitPrice);
    }

    public boolean delete(long id) {
        return jdbc.update("DELETE FROM line_items WHERE id = ?", id) > 0;
    }
}
