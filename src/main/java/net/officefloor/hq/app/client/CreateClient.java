package net.officefloor.hq.app.client;

import java.util.regex.Pattern;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/clients — add a client with a name and a proper email. */
public class CreateClient {

    /** A client needs a proper email; same shape the UI enforces (see feature.tsx). */
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public void service(@RequestBody NewClient body, ClientRepository repository,
            ObjectResponse<Client> response) {
        String email = body.getEmail() == null ? "" : body.getEmail().trim();
        if (!EMAIL.matcher(email).matches()) {
            throw new HttpException(HttpStatus.BAD_REQUEST,
                    "A client requires a valid email address");
        }
        // Two clients cannot share an email; reject a duplicate before it reaches the unique
        // constraint (see V24__clients_email_unique.sql).
        if (repository.existsByEmail(email)) {
            throw new HttpException(HttpStatus.CONFLICT,
                    "A client with this email already exists");
        }
        Client client = new Client();
        client.setName(body.getName());
        client.setEmail(email);
        response.send(repository.save(client));
    }
}
