package net.officefloor.hq.app;

import java.util.regex.Pattern;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * PUT /api/clients/{clientId} — correct a client's name and email from the list, appends one audit
 * record (CLIENT_UPDATED id=&lt;id&gt;) so there is a record it happened, and echoes back the saved
 * client. An unknown client is rejected with 400 and nothing is written. The email must carry a
 * proper shape (blank or malformed is rejected with 400, mirroring the UI and schema checks in V2),
 * and must not already belong to ANOTHER client (rejected with 409, schema check in V28) — the
 * client may of course keep its own email.
 */
public class UpdateClientLogic {

    /** Basic email shape: something, '@', something, '.', something — no whitespace. */
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public void service(@HttpPathParameter("clientId") String clientId,
            @RequestBody NewClient newClient, ClientRepository clients, Audit audit,
            ObjectResponse<Client> response) {
        Long id = Long.valueOf(clientId);
        Client client = clients.findById(id).orElseThrow(
                () -> new HttpException(HttpStatus.BAD_REQUEST, "An existing client is required"));
        String email = newClient.getEmail();
        if (email == null || !EMAIL.matcher(email.trim()).matches()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A proper email address is required");
        }
        String trimmed = email.trim();
        if (clients.existsByEmailIgnoreCaseAndIdNot(trimmed, id)) {
            throw new HttpException(HttpStatus.CONFLICT,
                    "A client with this email already exists");
        }
        client.setName(newClient.getName());
        client.setEmail(trimmed);
        Client saved = clients.save(client);
        audit.record("CLIENT_UPDATED id=" + id);
        response.send(saved);
    }
}
