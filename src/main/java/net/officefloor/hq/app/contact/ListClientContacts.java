package net.officefloor.hq.app.contact;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/** GET /api/clients/{id}/contacts — every contact kept for one client, oldest first. */
public class ListClientContacts {

    public void service(@HttpPathParameter("id") String id, JdbcTemplate jdbc,
            ObjectResponse<List<ContactView>> response) {
        List<ContactView> contacts = jdbc.query(
                "SELECT id, client_id, name, email, role FROM contacts "
                        + "WHERE client_id = ? ORDER BY id",
                (rs, i) -> new ContactView(rs.getLong("id"), rs.getLong("client_id"),
                        rs.getString("name"), rs.getString("email"), rs.getString("role")),
                Long.valueOf(id));
        response.send(contacts);
    }
}
