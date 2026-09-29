package net.officefloor.hq.app;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** Data access for {@link Contact} rows (JdbcTemplate over the schema in V9__contacts.sql). */
@Repository
public class ContactRepository {

    private final JdbcTemplate jdbc;

    public ContactRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final org.springframework.jdbc.core.RowMapper<Contact> MAPPER =
            (rs, i) -> new Contact(rs.getLong("id"), rs.getLong("client_id"),
                    rs.getString("name"), rs.getString("email"), rs.getString("role"));

    /** Contacts for one client — used by the client detail view. */
    public List<Contact> findByClient(long clientId) {
        return jdbc.query(
                "SELECT id, client_id, name, email, role FROM contacts"
                        + " WHERE client_id = ? ORDER BY id",
                MAPPER, clientId);
    }

    public Contact create(long clientId, String name, String email, String role) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO contacts (client_id, name, email, role) VALUES (?, ?, ?, ?)",
                    new String[] {"id"});
            ps.setLong(1, clientId);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.setString(4, role);
            return ps;
        }, keys);
        return new Contact(keys.getKey().longValue(), clientId, name, email, role);
    }
}
