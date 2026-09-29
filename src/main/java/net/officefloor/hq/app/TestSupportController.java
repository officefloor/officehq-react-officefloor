package net.officefloor.hq.app;

import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Per-spec data setup for the harness (DESIGN.md §9). Profile-guarded so it exists ONLY under the
 * harness launch (bin/start sets spring.profiles.active=harness) — never in a real deploy. This is
 * APP CODE and EVOLVES with the schema (NOT pinned); a change that breaks a prior spec's seed is a
 * seed-path regression. Tests call these to ARRANGE data; they ASSERT only through the UI.
 */
@Profile("harness")
@RestController
@RequestMapping("/__test__")
public class TestSupportController {

    private final Audit audit;
    private final JdbcTemplate jdbc;

    public TestSupportController(Audit audit, JdbcTemplate jdbc) {
        this.audit = audit;
        this.jdbc = jdbc;
    }

    /** Truncate all domain tables and clear the audit file so each spec starts clean. */
    @PostMapping("/reset")
    public void reset() {
        audit.clear();
        // projects references clients (FK); H2 refuses to TRUNCATE an FK-referenced table, so lift
        // referential integrity for the duration of the reset.
        jdbc.execute("SET REFERENTIAL_INTEGRITY FALSE");
        try {
            jdbc.execute("TRUNCATE TABLE notes RESTART IDENTITY");
            jdbc.execute("TRUNCATE TABLE project_tags RESTART IDENTITY");
            jdbc.execute("TRUNCATE TABLE tags RESTART IDENTITY");
            jdbc.execute("TRUNCATE TABLE tasks RESTART IDENTITY");
            jdbc.execute("TRUNCATE TABLE line_items RESTART IDENTITY");
            jdbc.execute("TRUNCATE TABLE invoices RESTART IDENTITY");
            jdbc.execute("TRUNCATE TABLE contacts RESTART IDENTITY");
            jdbc.execute("TRUNCATE TABLE projects RESTART IDENTITY");
            jdbc.execute("TRUNCATE TABLE clients RESTART IDENTITY");
        } finally {
            jdbc.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }

    /** Insert the fixture a spec needs; the payload shape evolves with the schema. */
    @SuppressWarnings("unchecked")
    @PostMapping("/seed")
    public void seed(@RequestBody Map<String, Object> fixture) {
        List<Map<String, Object>> clients =
                (List<Map<String, Object>>) fixture.getOrDefault("clients", List.of());
        for (Map<String, Object> c : clients) {
            jdbc.update("INSERT INTO clients (id, name, email) VALUES (?, ?, ?)",
                    ((Number) c.get("id")).longValue(), c.get("name"), c.get("email"));
        }
        List<Map<String, Object>> projects =
                (List<Map<String, Object>>) fixture.getOrDefault("projects", List.of());
        for (Map<String, Object> p : projects) {
            jdbc.update("INSERT INTO projects (id, name, client_id) VALUES (?, ?, ?)",
                    ((Number) p.get("id")).longValue(), p.get("name"),
                    ((Number) p.get("clientId")).longValue());
        }
        List<Map<String, Object>> contacts =
                (List<Map<String, Object>>) fixture.getOrDefault("contacts", List.of());
        for (Map<String, Object> ct : contacts) {
            jdbc.update(
                    "INSERT INTO contacts (id, client_id, name, email, role) VALUES (?, ?, ?, ?, ?)",
                    ((Number) ct.get("id")).longValue(),
                    ((Number) ct.get("clientId")).longValue(),
                    ct.get("name"), ct.get("email"), ct.get("role"));
        }
        List<Map<String, Object>> tasks =
                (List<Map<String, Object>>) fixture.getOrDefault("tasks", List.of());
        for (Map<String, Object> t : tasks) {
            Object done = t.get("done");
            jdbc.update("INSERT INTO tasks (id, project_id, title, done) VALUES (?, ?, ?, ?)",
                    ((Number) t.get("id")).longValue(),
                    ((Number) t.get("projectId")).longValue(),
                    t.get("title"),
                    done != null && Boolean.parseBoolean(done.toString()));
        }
        List<Map<String, Object>> tags =
                (List<Map<String, Object>>) fixture.getOrDefault("tags", List.of());
        for (Map<String, Object> tg : tags) {
            jdbc.update("INSERT INTO tags (id, name) VALUES (?, ?)",
                    ((Number) tg.get("id")).longValue(), tg.get("name"));
        }
        List<Map<String, Object>> projectTags =
                (List<Map<String, Object>>) fixture.getOrDefault("projectTags", List.of());
        for (Map<String, Object> pt : projectTags) {
            jdbc.update("INSERT INTO project_tags (project_id, tag_id) VALUES (?, ?)",
                    ((Number) pt.get("projectId")).longValue(),
                    ((Number) pt.get("tagId")).longValue());
        }
        List<Map<String, Object>> notes =
                (List<Map<String, Object>>) fixture.getOrDefault("notes", List.of());
        for (Map<String, Object> n : notes) {
            Object at = n.get("at");
            jdbc.update(
                    "INSERT INTO notes (id, target_type, target_id, body, created_at)"
                            + " VALUES (?, ?, ?, ?, ?)",
                    ((Number) n.get("id")).longValue(),
                    n.get("targetType"),
                    ((Number) n.get("targetId")).longValue(),
                    n.get("text"),
                    at == null ? null : java.time.OffsetDateTime.parse(at.toString()));
        }
        if (!notes.isEmpty()) {
            // Inserting explicit ids into the IDENTITY column does not advance H2's generator, so a
            // later app-side INSERT would reuse id 1 and hit the PK. Bump the generator past the
            // seeded ids.
            Long nextNoteId =
                    jdbc.queryForObject("SELECT COALESCE(MAX(id), 0) + 1 FROM notes", Long.class);
            jdbc.execute("ALTER TABLE notes ALTER COLUMN id RESTART WITH " + nextNoteId);
        }
        List<Map<String, Object>> invoices =
                (List<Map<String, Object>>) fixture.getOrDefault("invoices", List.of());
        for (Map<String, Object> inv : invoices) {
            Object status = inv.get("status");
            Object issuedDate = inv.get("issuedDate");
            Object dueDate = inv.get("dueDate");
            // Amount is derived from the invoice's line items (V13), so it is not seeded directly;
            // the column defaults to 0.
            jdbc.update(
                    "INSERT INTO invoices (id, project_id, status, issued_date, due_date)"
                            + " VALUES (?, ?, ?, ?, ?)",
                    ((Number) inv.get("id")).longValue(),
                    ((Number) inv.get("projectId")).longValue(),
                    status == null ? "DRAFT" : status.toString(),
                    issuedDate == null ? null : java.sql.Date.valueOf(issuedDate.toString()),
                    dueDate == null ? null : java.sql.Date.valueOf(dueDate.toString()));
            List<Map<String, Object>> lineItems =
                    (List<Map<String, Object>>) inv.getOrDefault("lineItems", List.of());
            for (Map<String, Object> li : lineItems) {
                jdbc.update(
                        "INSERT INTO line_items (id, invoice_id, description, qty, unit_price)"
                                + " VALUES (?, ?, ?, ?, ?)",
                        ((Number) li.get("id")).longValue(),
                        ((Number) inv.get("id")).longValue(),
                        li.get("description"),
                        ((Number) li.get("qty")).intValue(),
                        new java.math.BigDecimal(li.get("unitPrice").toString()));
            }
        }
    }
}
