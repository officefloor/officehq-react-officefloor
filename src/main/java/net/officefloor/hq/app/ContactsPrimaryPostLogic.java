package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/contacts/primary — choose a client's one main contact. Sets the chosen contact primary
 * and clears any previous primary for the same client, then returns the client's contacts. Records
 * one audit line (CONTACT_MADE_PRIMARY clientId=&lt;id&gt; contactId=&lt;id&gt;).
 */
public class ContactsPrimaryPostLogic {

    public void service(@RequestBody SetPrimaryContact body, ContactRepository repository,
            Audit audit, ObjectResponse<List<Contact>> response) {
        if (body.getClientId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A client is required.");
        }
        if (body.getContactId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A contact is required.");
        }
        if (!repository.existsForClient(body.getClientId(), body.getContactId())) {
            throw new HttpException(HttpStatus.NOT_FOUND, "No such contact for this client.");
        }
        repository.setPrimary(body.getClientId(), body.getContactId());
        audit.record("CONTACT_MADE_PRIMARY clientId=" + body.getClientId()
                + " contactId=" + body.getContactId());
        response.send(repository.findByClient(body.getClientId()));
    }
}
