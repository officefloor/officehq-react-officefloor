package net.officefloor.hq.app;

/**
 * A label the owner puts on projects to group them. Serialised as JSON by the /api/tags and
 * /api/projects/tags routes; the front-end renders each as a chip.
 */
public record Tag(long id, String name) {
}
