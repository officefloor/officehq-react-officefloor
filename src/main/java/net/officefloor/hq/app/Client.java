package net.officefloor.hq.app;

/** A client the owner tracks. Serialised as JSON by the /api/clients routes. */
public record Client(long id, String name, String email) {
}
