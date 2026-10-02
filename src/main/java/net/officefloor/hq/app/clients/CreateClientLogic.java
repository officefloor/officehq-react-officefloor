package net.officefloor.hq.app.clients;

import net.officefloor.web.ObjectResponse;

/** POST /api/clients — add a client from the JSON body and return the result. */
public class CreateClientLogic {

    public void service(NewClient body, ClientService clients,
            ObjectResponse<CreateClientResult> response) {
        try {
            Client saved = clients.create(body.getName(), body.getEmail());
            response.send(CreateClientResult
                    .created(new ClientView(saved.getId(), saved.getName(), saved.getEmail())));
        } catch (DuplicateClientEmailException e) {
            // Email already in use: no row added; the UI surfaces the email error.
            response.send(CreateClientResult.emailInUse());
        }
    }
}
