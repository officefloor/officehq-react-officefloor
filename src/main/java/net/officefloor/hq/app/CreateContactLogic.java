package net.officefloor.hq.app;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/clients/{clientId}/contacts — add a contact (name, email, role) to an existing client,
 * echo back the saved row. An unknown client is rejected with 400 and never persisted.
 */
public class CreateContactLogic {

    public void service(@HttpPathParameter("clientId") String clientId,
            @RequestBody NewContact newContact, ContactRepository contacts,
            ClientRepository clients, ObjectResponse<ContactView> response) {
        Long id = Long.valueOf(clientId);
        clients.findById(id).orElseThrow(
                () -> new HttpException(HttpStatus.BAD_REQUEST, "An existing client is required"));
        Contact saved = contacts.save(new Contact(id, newContact.getName(), newContact.getEmail(),
                newContact.getRole()));
        response.send(new ContactView(saved.getId(), saved.getClientId(), saved.getName(),
                saved.getEmail(), saved.getRole()));
    }
}
