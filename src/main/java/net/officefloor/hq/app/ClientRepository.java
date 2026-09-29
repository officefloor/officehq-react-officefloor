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

    public List<Client> findAll() {
        return jdbc.query(
                "SELECT id, name, email FROM clients ORDER BY id",
                (rs, i) -> new Client(rs.getLong("id"), rs.getString("name"), rs.getString("email")));
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
        return new Client(keys.getKey().longValue(), name, email);
    }
}
