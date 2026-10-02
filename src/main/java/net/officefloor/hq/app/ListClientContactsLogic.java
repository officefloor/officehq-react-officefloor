package net.officefloor.hq.app;

import java.util.List;
import java.util.stream.Collectors;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/clients/{clientId}/contacts — the contacts for one client, oldest first. */
public class ListClientContactsLogic {

    public void service(@HttpPathParameter("clientId") String clientId,
            ContactRepository contacts, ObjectResponse<List<ContactView>> response) {
        Long id = Long.valueOf(clientId);
        List<ContactView> views = contacts.findByClientIdOrderByIdAsc(id).stream()
                .map(c -> new ContactView(c.getId(), c.getClientId(), c.getName(), c.getEmail(),
                        c.getRole()))
                .collect(Collectors.toList());
        response.send(views);
    }
}
