package net.officefloor.hq.app;

/**
 * At-a-glance counts for a single client's detail view: how many projects and contacts belong to
 * that client. Serialised as JSON by GET /api/clients/summary?clientId=&lt;id&gt;.
 */
public record ClientSummary(long projectsCount, long contactsCount) {
}
