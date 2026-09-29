package net.officefloor.hq.app.contact;

import net.officefloor.hq.app.Audit;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * POST /api/contacts/{id}/primary — make this contact the client's one main contact. A client keeps
 * at most one primary, so the flag is cleared across the contact's client before it is set on this
 * one. Records the change to the audit file, and returns the now-primary contact.
 */
public class SetPrimaryContact {

    public void service(@HttpPathParameter("id") String id, JdbcTemplate jdbc, Audit audit,
            ObjectResponse<ContactView> response) {
        Long contactId = Long.valueOf(id);
        Long clientId;
        try {
            clientId = jdbc.queryForObject("SELECT client_id FROM contacts WHERE id = ?",
                    Long.class, contactId);
        } catch (EmptyResultDataAccessException e) {
            throw new HttpException(HttpStatus.NOT_FOUND, "No such contact");
        }
        // One main contact per client: clear the flag across the client, then set it on this one.
        jdbc.update("UPDATE contacts SET is_primary = FALSE WHERE client_id = ?", clientId);
        jdbc.update("UPDATE contacts SET is_primary = TRUE WHERE id = ?", contactId);
        audit.record("CLIENT_PRIMARY_CONTACT_SET client=" + clientId + " contact=" + contactId);
        ContactView view = jdbc.queryForObject(
                "SELECT id, client_id, name, email, role, is_primary FROM contacts WHERE id = ?",
                (rs, i) -> new ContactView(rs.getLong("id"), rs.getLong("client_id"),
                        rs.getString("name"), rs.getString("email"), rs.getString("role"),
                        rs.getBoolean("is_primary")),
                contactId);
        response.send(view);
    }
}
