package net.officefloor.hq.app.contact;

import java.util.regex.Pattern;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/clients/{id}/contacts — add a contact (name + email + role) to a client. */
public class CreateContact {

    /** A contact needs a proper email; same shape the UI enforces (see feature.tsx). */
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public void service(@HttpPathParameter("id") String id, @RequestBody NewContact body,
            ContactRepository repository, ObjectResponse<Contact> response) {
        String name = body.getName() == null ? "" : body.getName().trim();
        String email = body.getEmail() == null ? "" : body.getEmail().trim();
        String role = body.getRole() == null ? "" : body.getRole().trim();
        if (name.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A contact requires a name");
        }
        if (email.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A contact requires an email");
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new HttpException(HttpStatus.BAD_REQUEST,
                    "A contact requires a valid email address");
        }
        if (role.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A contact requires a role");
        }
        Contact contact = new Contact();
        contact.setClientId(Long.valueOf(id));
        contact.setName(name);
        contact.setEmail(email);
        contact.setRole(role);
        response.send(repository.save(contact));
    }
}
