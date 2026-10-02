package net.officefloor.hq.app;

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/clients/{clientId}/summary — how many projects and contacts one client has. */
public class ClientSummaryLogic {

    public void service(@HttpPathParameter("clientId") String clientId,
            ProjectRepository projects, ContactRepository contacts,
            ObjectResponse<ClientSummaryView> response) {
        Long id = Long.valueOf(clientId);
        long projectsCount = projects.countByClientId(id);
        long contactsCount = contacts.countByClientId(id);
        response.send(new ClientSummaryView(projectsCount, contactsCount));
    }
}
