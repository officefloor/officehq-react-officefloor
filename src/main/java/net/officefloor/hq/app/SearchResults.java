package net.officefloor.hq.app;

import java.util.List;

/**
 * The result of the one global search box: matches across both entity kinds, grouped so the UI can
 * show a "Clients" group and a "Projects" group. Serialised as JSON by GET /api/search.
 */
public record SearchResults(List<Client> clients, List<Project> projects) {
}
