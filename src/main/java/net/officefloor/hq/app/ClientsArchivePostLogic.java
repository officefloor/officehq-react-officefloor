package net.officefloor.hq.app;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/clients/archive — tuck a client away instead of deleting it. Flags the row archived so
 * it drops off the main client list and the name search, but keeps the row so nothing is lost.
 * Records one audit line (CLIENT_ARCHIVED id=&lt;id&gt;) so it is noted.
 */
public class ClientsArchivePostLogic {

    public void service(@RequestBody ArchiveClient body, ClientRepository repository, Audit audit,
            ObjectResponse<Client> response) {
        if (body.getId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A client id is required.");
        }
        Client existing = repository.findById(body.getId());
        if (existing == null) {
            throw new HttpException(HttpStatus.NOT_FOUND, "No such client.");
        }
        repository.archive(existing.id());
        audit.record("CLIENT_ARCHIVED id=" + existing.id());
        response.send(existing);
    }
}
