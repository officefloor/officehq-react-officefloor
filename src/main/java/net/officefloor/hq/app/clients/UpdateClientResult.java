package net.officefloor.hq.app.clients;

/**
 * JSON response shape for correcting a client. Either the row was updated ({@code client} set), or
 * it was rejected because another client already uses that email ({@code emailInUse} true) — the UI
 * branches on this to surface the duplicate-email error without changing the row.
 */
public class UpdateClientResult {

    private final ClientView client;
    private final boolean emailInUse;

    private UpdateClientResult(ClientView client, boolean emailInUse) {
        this.client = client;
        this.emailInUse = emailInUse;
    }

    public static UpdateClientResult updated(ClientView client) {
        return new UpdateClientResult(client, false);
    }

    public static UpdateClientResult emailInUse() {
        return new UpdateClientResult(null, true);
    }

    public ClientView getClient() {
        return client;
    }

    public boolean isEmailInUse() {
        return emailInUse;
    }
}
