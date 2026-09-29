package net.officefloor.hq.app;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/clients/restore — bring a tucked-away client back. Clears the archived flag so the row
 * returns to the main client list and the name search. Records one audit line
 * (CLIENT_RESTORED id=&lt;id&gt;) so it is noted, mirroring the archive path.
 */
public class ClientsRestorePostLogic {

    public void service(@RequestBody RestoreClient body, ClientRepository repository, Audit audit,
            ObjectResponse<Client> response) {
        if (body.getId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A client id is required.");
        }
        Client existing = repository.findById(body.getId());
        if (existing == null) {
            throw new HttpException(HttpStatus.NOT_FOUND, "No such client.");
        }
        repository.restore(existing.id());
        audit.record("CLIENT_RESTORED id=" + existing.id());
        response.send(existing);
    }
}
