package net.officefloor.hq.app.client;

import java.util.regex.Pattern;
import net.officefloor.hq.app.Audit;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * PUT /api/clients/{id} — correct a client's name and/or email. Same email shape the UI and
 * {@link CreateClient} enforce, and the same rule that two clients cannot share an email (the
 * client's own current email is of course allowed). Records the change to the audit file and
 * returns the updated client.
 */
public class UpdateClient {

    /** A client needs a proper email; same shape the UI enforces (see feature.tsx). */
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public void service(@HttpPathParameter("id") String id, @RequestBody NewClient body,
            ClientRepository repository, Audit audit, ObjectResponse<Client> response) {
        Long clientId = Long.valueOf(id);
        Client client = repository.findById(clientId)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "No such client"));
        String name = body.getName() == null ? "" : body.getName().trim();
        String email = body.getEmail() == null ? "" : body.getEmail().trim();
        if (name.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A client requires a name");
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new HttpException(HttpStatus.BAD_REQUEST,
                    "A client requires a valid email address");
        }
        // Two clients cannot share an email; a duplicate against ANOTHER client is rejected, but the
        // client keeping its own current email is fine.
        if (repository.existsByEmailAndIdNot(email, clientId)) {
            throw new HttpException(HttpStatus.CONFLICT,
                    "A client with this email already exists");
        }
        client.setName(name);
        client.setEmail(email);
        Client saved = repository.save(client);
        audit.record("CLIENT_UPDATED id=" + saved.getId());
        response.send(saved);
    }
}
