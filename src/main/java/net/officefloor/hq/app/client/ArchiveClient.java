package net.officefloor.hq.app.client;

import net.officefloor.hq.app.Audit;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/clients/{id}/archive — tuck a client away instead of deleting it: flag it archived so it
 * drops off the client list and out of the client search, but nothing is lost (the row is retained).
 * Records the fact to the audit file so it can be checked back later.
 */
public class ArchiveClient {

    public void service(@HttpPathParameter("id") String id, ClientRepository repository,
            Audit audit, ObjectResponse<Client> response) {
        Long clientId = Long.valueOf(id);
        Client client = repository.findById(clientId)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "No such client"));
        client.setArchived(true);
        Client saved = repository.save(client);
        audit.record("CLIENT_ARCHIVED id=" + saved.getId());
        response.send(saved);
    }
}
