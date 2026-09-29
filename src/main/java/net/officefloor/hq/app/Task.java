package net.officefloor.hq.app;

/**
 * A task on a project's checklist. Serialised as JSON by the /api/tasks routes; {@code done}
 * flips as the owner ticks it off (OPEN &lt;-&gt; DONE in the UI).
 */
public record Task(long id, long projectId, String title, boolean done) {
}
