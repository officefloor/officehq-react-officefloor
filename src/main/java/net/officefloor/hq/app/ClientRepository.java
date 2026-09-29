package net.officefloor.hq.app;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** Data access for {@link Client} rows (JdbcTemplate over the H2 schema in V1__clients.sql). */
@Repository
public class ClientRepository {

    private final JdbcTemplate jdbc;

    public ClientRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * List clients. Archived clients are hidden — that is what keeps a tucked-away client off the
     * main list (and the name search that filters it) while its row is retained.
     */
    public List<Client> findAll() {
        return jdbc.query(
                "SELECT id, name, email, currency FROM clients WHERE archived = FALSE ORDER BY id",
                MAPPER);
    }

    /** Maps a client row (name/email plus the currency their money is shown in). */
    private static final org.springframework.jdbc.core.RowMapper<Client> MAPPER =
            (rs, i) -> new Client(rs.getLong("id"), rs.getString("name"), rs.getString("email"),
                    rs.getString("currency"));

    /**
     * Clients whose name matches the global search term (case-insensitive substring). Archived
     * clients stay hidden, matching the plain list. A blank term matches nothing — the global
     * search only surfaces results once something is typed.
     */
    public List<Client> searchByName(String term) {
        if (term == null || term.isBlank()) {
            return List.of();
        }
        return jdbc.query(
                "SELECT id, name, email, currency FROM clients"
                        + " WHERE archived = FALSE AND LOWER(name) LIKE ? ORDER BY id",
                MAPPER,
                "%" + term.trim().toLowerCase() + "%");
    }

    /** Look up one client, or null when there is no such row. */
    public Client findById(long id) {
        List<Client> rows = jdbc.query(
                "SELECT id, name, email, currency FROM clients WHERE id = ?",
                MAPPER,
                id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * List the archived (tucked-away) clients, so they can be reviewed and brought back. The inverse
     * of {@link #findAll()}: only rows flagged archived, in the same id order.
     */
    public List<Client> findArchived() {
        return jdbc.query(
                "SELECT id, name, email, currency FROM clients WHERE archived = TRUE ORDER BY id",
                MAPPER);
    }

    /** Tuck a client away: flag it archived so it drops off the lists but its row is retained. */
    public boolean archive(long id) {
        return jdbc.update("UPDATE clients SET archived = TRUE WHERE id = ?", id) > 0;
    }

    /** Bring an archived client back: clear the flag so it returns to the main list and search. */
    public boolean restore(long id) {
        return jdbc.update("UPDATE clients SET archived = FALSE WHERE id = ?", id) > 0;
    }

    /** Correct a client's name/email. Returns the updated row, or null when there is no such row. */
    public Client update(long id, String name, String email) {
        int rows = jdbc.update("UPDATE clients SET name = ?, email = ? WHERE id = ?", name, email, id);
        return rows > 0 ? findById(id) : null;
    }

    /**
     * Set the currency a client is paid in (their money is shown in it everywhere). Returns the
     * updated row, or null when there is no such client.
     */
    public Client updateCurrency(long id, String currency) {
        int rows = jdbc.update("UPDATE clients SET currency = ? WHERE id = ?", currency, id);
        return rows > 0 ? findById(id) : null;
    }

    /**
     * Whether a client already uses this email. Two clients cannot share an email, so a create must
     * be rejected when one exists (defence alongside the {@code clients_email_unique} constraint in
     * V27). Considers every row, including archived ones — the email is still taken.
     */
    public boolean existsByEmail(String email) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM clients WHERE email = ?", Integer.class, email);
        return count != null && count > 0;
    }

    /**
     * Whether a client OTHER than {@code id} already uses this email. Lets a client keep its own
     * address on an edit while still rejecting a collision with a different client (mirrors the
     * {@code clients_email_unique} constraint in V27).
     */
    public boolean existsByEmailForOther(String email, long id) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM clients WHERE email = ? AND id <> ?", Integer.class, email, id);
        return count != null && count > 0;
    }

    public Client create(String name, String email) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO clients (name, email) VALUES (?, ?)", new String[] {"id"});
            ps.setString(1, name);
            ps.setString(2, email);
            return ps;
        }, keys);
        // A new client starts on the app's default currency (USD, the column default); the owner
        // can set another afterwards.
        return new Client(keys.getKey().longValue(), name, email, "USD");
    }
}
