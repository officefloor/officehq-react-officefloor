package net.officefloor.hq.app.client;

import net.officefloor.hq.app.Audit;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/clients/{id}/restore — bring a tucked-away client back: clear its archived flag so it
 * returns to the client list (and the client search) alongside the clients still in play. The mirror
 * of {@link ArchiveClient}. Records the fact to the audit file so it can be checked back later.
 */
public class RestoreClient {

    public void service(@HttpPathParameter("id") String id, ClientRepository repository,
            Audit audit, ObjectResponse<Client> response) {
        Long clientId = Long.valueOf(id);
        Client client = repository.findById(clientId)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "No such client"));
        client.setArchived(false);
        Client saved = repository.save(client);
        audit.record("CLIENT_RESTORED id=" + saved.getId());
        response.send(saved);
    }
}
