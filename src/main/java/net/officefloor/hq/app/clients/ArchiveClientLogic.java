package net.officefloor.hq.app.clients;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/clients/{clientId}/archive — tuck a client away rather than deleting it. The row is kept
 * but flagged archived so it drops off the list and the search; the action is recorded in the audit
 * log. The remaining (non-archived) clients are returned.
 */
public class ArchiveClientLogic {

    public void service(@HttpPathParameter("clientId") String clientId, ClientService clients,
            ObjectResponse<List<ClientView>> response) {
        List<ClientView> views = clients.archive(Long.valueOf(clientId)).stream()
                .map(c -> new ClientView(c.getId(), c.getName(), c.getEmail(), c.getCurrency()))
                .toList();
        response.send(views);
    }
}
