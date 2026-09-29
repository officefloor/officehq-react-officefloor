package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** Data access for {@link Payment} rows (JdbcTemplate over the schema in V18__payments.sql). */
@Repository
public class PaymentRepository {

    private final JdbcTemplate jdbc;

    public PaymentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final org.springframework.jdbc.core.RowMapper<Payment> MAPPER =
            (rs, i) -> new Payment(rs.getLong("id"), rs.getLong("invoice_id"),
                    rs.getBigDecimal("amount"), rs.getString("paid_date"));

    public List<Payment> findByInvoice(long invoiceId) {
        return jdbc.query(
                "SELECT id, invoice_id, amount, paid_date FROM payments"
                        + " WHERE invoice_id = ? ORDER BY id",
                MAPPER, invoiceId);
    }

    public Payment create(long invoiceId, BigDecimal amount, String date) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO payments (invoice_id, amount, paid_date) VALUES (?, ?, ?)",
                    new String[] {"id"});
            ps.setLong(1, invoiceId);
            ps.setBigDecimal(2, amount);
            ps.setDate(3, Date.valueOf(date));
            return ps;
        }, keys);
        return new Payment(keys.getKey().longValue(), invoiceId, amount, date);
    }
}
