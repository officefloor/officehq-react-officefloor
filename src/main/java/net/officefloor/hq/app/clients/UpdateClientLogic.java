package net.officefloor.hq.app.clients;

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** POST /api/clients/{clientId} — correct a client's name and/or email and return the result. */
public class UpdateClientLogic {

    public void service(@HttpPathParameter("clientId") String clientId, NewClient body,
            ClientService clients, ObjectResponse<UpdateClientResult> response) {
        try {
            Client saved = clients.update(Long.valueOf(clientId), body.getName(), body.getEmail());
            response.send(UpdateClientResult
                    .updated(new ClientView(saved.getId(), saved.getName(), saved.getEmail(),
                            saved.getCurrency())));
        } catch (DuplicateClientEmailException e) {
            // Email already in use by another client: the row is unchanged; the UI surfaces the error.
            response.send(UpdateClientResult.emailInUse());
        }
    }
}
