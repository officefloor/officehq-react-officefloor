package net.officefloor.hq.app;

/**
 * A client the owner tracks. Serialised as JSON by the /api/clients routes. Each client is paid in
 * their own {@code currency} (an ISO code such as {@code USD} or {@code EUR}); their money is shown
 * in it everywhere.
 */
public record Client(long id, String name, String email, String currency) {
}
