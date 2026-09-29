package net.officefloor.hq.app;

import java.util.regex.Pattern;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/clients — create a client from the request body. */
public class ClientsPostLogic {

    // Every client needs a proper email address. Kept in sync with the front-end check in
    // ClientsPage.tsx and the DB CHECK constraint (V2__clients_email_check.sql).
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@RequestBody NewClient body, ClientRepository repository,
            ObjectResponse<Client> response) {
        String email = body.getEmail() == null ? "" : body.getEmail().trim();
        if (!EMAIL.matcher(email).matches()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A valid email address is required.");
        }
        // Two clients cannot share an email — reject the create when the address is already in use
        // (mirrors the front-end check in ClientsPage.tsx and the clients_email_unique constraint).
        if (repository.existsByEmail(email)) {
            throw new HttpException(HttpStatus.CONFLICT, "A client with this email already exists.");
        }
        response.send(repository.create(body.getName(), email));
    }
}
