package net.officefloor.hq.app;

import java.util.regex.Pattern;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/clients — add a client from the submitted name + email, echo back the saved row.
 * A client must carry a proper email: a blank or malformed address is rejected with 400 and never
 * persisted (mirrors the UI validation and the schema check in V2).
 */
public class CreateClientLogic {

    /** Basic email shape: something, '@', something, '.', something — no whitespace. */
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public void service(@RequestBody NewClient newClient, ClientRepository clients,
            ObjectResponse<Client> response) {
        String email = newClient.getEmail();
        if (email == null || !EMAIL.matcher(email.trim()).matches()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A proper email address is required");
        }
        Client saved = clients.save(new Client(newClient.getName(), email.trim()));
        response.send(saved);
    }
}
