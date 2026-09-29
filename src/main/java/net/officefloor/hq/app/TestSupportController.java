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
        // projects references clients; drop referential integrity so both tables can be truncated
        // (and their identity counters restarted) regardless of FK order.
        jdbc.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbc.execute("TRUNCATE TABLE project_tags RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE tags RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE tasks RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE line_items RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE invoices RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE contacts RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE projects RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE clients RESTART IDENTITY");
        jdbc.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    /** Insert the fixture a spec needs; the payload shape evolves with the schema. */
    @PostMapping("/seed")
    @SuppressWarnings("unchecked")
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
                    ((Number) ct.get("clientId")).longValue(), ct.get("name"), ct.get("email"),
                    ct.get("role"));
        }
        List<Map<String, Object>> invoices =
                (List<Map<String, Object>>) fixture.getOrDefault("invoices", List.of());
        for (Map<String, Object> in : invoices) {
            Object status = in.get("status");
            Object amount = in.get("amount");
            Object issuedDate = in.get("issuedDate");
            Object dueDate = in.get("dueDate");
            // Insert only the columns the fixture supplies, so the table's own defaults apply when a
            // fixture omits them (a plain null would trip the NOT NULL columns). status defaults to
            // DRAFT and amount defaults to 0 — an invoice's amount is now derived from its line
            // items, so fixtures list lineItems rather than a single typed amount.
            StringBuilder cols = new StringBuilder("id, project_id, status");
            StringBuilder marks = new StringBuilder("?, ?, ?");
            List<Object> args = new java.util.ArrayList<>(List.of(
                    ((Number) in.get("id")).longValue(),
                    ((Number) in.get("projectId")).longValue(),
                    status == null ? "DRAFT" : status.toString()));
            if (amount != null) {
                cols.append(", amount");
                marks.append(", ?");
                args.add(((Number) amount).doubleValue());
            }
            if (issuedDate != null) {
                cols.append(", issued_date");
                marks.append(", ?");
                args.add(issuedDate.toString());
            }
            if (dueDate != null) {
                cols.append(", due_date");
                marks.append(", ?");
                args.add(dueDate.toString());
            }
            jdbc.update("INSERT INTO invoices (" + cols + ") VALUES (" + marks + ")",
                    args.toArray());
            List<Map<String, Object>> lineItems =
                    (List<Map<String, Object>>) in.getOrDefault("lineItems", List.of());
            for (Map<String, Object> li : lineItems) {
                jdbc.update(
                        "INSERT INTO line_items (id, invoice_id, description, qty, unit_price)"
                                + " VALUES (?, ?, ?, ?, ?)",
                        ((Number) li.get("id")).longValue(),
                        ((Number) in.get("id")).longValue(), li.get("description"),
                        ((Number) li.get("qty")).intValue(),
                        ((Number) li.get("unitPrice")).doubleValue());
            }
        }
        List<Map<String, Object>> tasks =
                (List<Map<String, Object>>) fixture.getOrDefault("tasks", List.of());
        for (Map<String, Object> t : tasks) {
            jdbc.update("INSERT INTO tasks (id, project_id, title, done) VALUES (?, ?, ?, ?)",
                    ((Number) t.get("id")).longValue(),
                    ((Number) t.get("projectId")).longValue(), t.get("title"),
                    Boolean.TRUE.equals(t.get("done")));
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
    }
}
