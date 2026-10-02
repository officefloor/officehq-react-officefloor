package net.officefloor.hq.app;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/clients/{clientId}/restore — bring a tucked-away client back. Clears the client's
 * archived flag so it returns to the main clients list and search, appends one audit record
 * (CLIENT_RESTORED id=&lt;id&gt;) so there is a record it happened, and echoes back the saved client.
 * An unknown client is rejected with 400 and nothing is written.
 */
public class RestoreClientLogic {

    public void service(@HttpPathParameter("clientId") String clientId, ClientRepository clients,
            Audit audit, ObjectResponse<Client> response) {
        Long id = Long.valueOf(clientId);
        Client client = clients.findById(id).orElseThrow(
                () -> new HttpException(HttpStatus.BAD_REQUEST, "An existing client is required"));
        client.setArchived(false);
        Client saved = clients.save(client);
        audit.record("CLIENT_RESTORED id=" + id);
        response.send(saved);
    }
}
