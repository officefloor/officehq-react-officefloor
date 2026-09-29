package net.officefloor.hq.app;

import java.util.regex.Pattern;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/contacts — add a contact (name, email, role) to a client from the request body. */
public class ContactsPostLogic {

    // Every contact needs a proper email address. Kept in sync with the front-end check in
    // ClientContacts.tsx and the DB CHECK constraint (V10__contacts_email_check.sql).
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@RequestBody NewContact body, ContactRepository repository,
            ObjectResponse<Contact> response) {
        if (body.getClientId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A client is required.");
        }
        String name = body.getName() == null ? "" : body.getName().trim();
        String email = body.getEmail() == null ? "" : body.getEmail().trim();
        String role = body.getRole() == null ? "" : body.getRole().trim();
        if (name.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A contact name is required.");
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A valid contact email is required.");
        }
        if (role.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A contact role is required.");
        }
        response.send(repository.create(body.getClientId(), name, email, role));
    }
}
