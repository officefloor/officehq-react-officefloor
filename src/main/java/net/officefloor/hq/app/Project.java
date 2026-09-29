package net.officefloor.hq.app;

/**
 * A project the owner does for a client. Serialised as JSON by the /api/projects routes; carries the
 * client's NAME (joined) so the UI can show it without a second lookup.
 */
public record Project(long id, String name, long clientId, String clientName) {
}
