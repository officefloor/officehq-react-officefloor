package net.officefloor.hq.app;

/**
 * A free-text note the owner wrote on a target (a project). Serialised as JSON by the
 * /api/projects/notes routes; the front-end renders each as a row, newest first.
 */
public record Note(long id, String text) {
}
