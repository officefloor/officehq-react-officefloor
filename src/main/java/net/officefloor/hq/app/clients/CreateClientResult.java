package net.officefloor.hq.app.clients;

/**
 * JSON response shape for creating a client. Either the row was created ({@code client} set), or it
 * was rejected because another client already uses that email ({@code emailInUse} true) — the UI
 * branches on this to surface the duplicate-email error without adding a row.
 */
public class CreateClientResult {

    private final ClientView client;
    private final boolean emailInUse;

    private CreateClientResult(ClientView client, boolean emailInUse) {
        this.client = client;
        this.emailInUse = emailInUse;
    }

    public static CreateClientResult created(ClientView client) {
        return new CreateClientResult(client, false);
    }

    public static CreateClientResult emailInUse() {
        return new CreateClientResult(null, true);
    }

    public ClientView getClient() {
        return client;
    }

    public boolean isEmailInUse() {
        return emailInUse;
    }
}
