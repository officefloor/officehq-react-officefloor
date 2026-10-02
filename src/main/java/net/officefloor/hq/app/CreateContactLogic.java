package net.officefloor.hq.app;

import java.util.regex.Pattern;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/clients/{clientId}/contacts — add a contact (name, email, role) to an existing client,
 * echo back the saved row. An unknown client is rejected with 400 and never persisted. A contact
 * must carry a proper email: a blank or malformed address is rejected with 400 and never persisted
 * (mirrors the UI validation and the schema check in V12).
 */
public class CreateContactLogic {

    /** Basic email shape: something, '@', something, '.', something — no whitespace. */
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public void service(@HttpPathParameter("clientId") String clientId,
            @RequestBody NewContact newContact, ContactRepository contacts,
            ClientRepository clients, ObjectResponse<ContactView> response) {
        Long id = Long.valueOf(clientId);
        clients.findById(id).orElseThrow(
                () -> new HttpException(HttpStatus.BAD_REQUEST, "An existing client is required"));
        String email = newContact.getEmail();
        if (email == null || !EMAIL.matcher(email.trim()).matches()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A proper email address is required");
        }
        Contact saved = contacts.save(new Contact(id, newContact.getName(), email.trim(),
                newContact.getRole()));
        response.send(new ContactView(saved.getId(), saved.getClientId(), saved.getName(),
                saved.getEmail(), saved.getRole()));
    }
}
