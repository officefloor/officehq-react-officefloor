package net.officefloor.hq.app;

/**
 * A person to reach at a client (name, email, role). Serialised as JSON by the /api/contacts
 * routes; carries its client_id so the UI can list a single client's contacts.
 */
public record Contact(long id, long clientId, String name, String email, String role) {
}
