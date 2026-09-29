package net.officefloor.hq.app;

import java.util.regex.Pattern;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/clients/update — correct a client's name or email from the list. Validates the email
 * the same way a create does (format + no collision with another client), then updates the row and
 * records one audit line (CLIENT_UPDATED id=&lt;id&gt;).
 */
public class ClientsUpdatePostLogic {

    // Mirrors the check in ClientsPostLogic, the front-end validateEmail, and the V2 CHECK constraint.
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@RequestBody UpdateClient body, ClientRepository repository, Audit audit,
            ObjectResponse<Client> response) {
        if (body.getId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A client id is required.");
        }
        Client existing = repository.findById(body.getId());
        if (existing == null) {
            throw new HttpException(HttpStatus.NOT_FOUND, "No such client.");
        }
        String email = body.getEmail() == null ? "" : body.getEmail().trim();
        if (!EMAIL.matcher(email).matches()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A valid email address is required.");
        }
        // A client keeps its own email; only a collision with a different client is a conflict.
        if (repository.existsByEmailForOther(email, existing.id())) {
            throw new HttpException(HttpStatus.CONFLICT, "A client with this email already exists.");
        }
        Client updated = repository.update(existing.id(), body.getName(), email);
        audit.record("CLIENT_UPDATED id=" + existing.id());
        response.send(updated);
    }
}
