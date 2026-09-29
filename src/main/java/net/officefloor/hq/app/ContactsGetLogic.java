package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/contacts?clientId=&lt;id&gt; — list the contacts kept for one client.
 */
public class ContactsGetLogic {

    public void service(@HttpQueryParameter("clientId") String clientId,
            ContactRepository repository, ObjectResponse<List<Contact>> response) {
        if (clientId == null || clientId.isBlank()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A clientId is required.");
        }
        response.send(repository.findByClient(Long.parseLong(clientId.trim())));
    }
}
