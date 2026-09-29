package net.officefloor.hq.app;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/contacts — add a contact (name, email, role) to a client from the request body. */
public class ContactsPostLogic {

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
        if (email.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A contact email is required.");
        }
        if (role.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A contact role is required.");
        }
        response.send(repository.create(body.getClientId(), name, email, role));
    }
}
