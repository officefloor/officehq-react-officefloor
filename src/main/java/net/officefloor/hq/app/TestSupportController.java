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
            jdbc.execute("TRUNCATE TABLE tasks RESTART IDENTITY");
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
        List<Map<String, Object>> invoices =
                (List<Map<String, Object>>) fixture.getOrDefault("invoices", List.of());
        for (Map<String, Object> inv : invoices) {
            Object status = inv.get("status");
            Object issuedDate = inv.get("issuedDate");
            Object dueDate = inv.get("dueDate");
            jdbc.update(
                    "INSERT INTO invoices (id, project_id, amount, status, issued_date, due_date)"
                            + " VALUES (?, ?, ?, ?, ?, ?)",
                    ((Number) inv.get("id")).longValue(),
                    ((Number) inv.get("projectId")).longValue(),
                    new java.math.BigDecimal(inv.get("amount").toString()),
                    status == null ? "UNPAID" : status.toString(),
                    issuedDate == null ? null : java.sql.Date.valueOf(issuedDate.toString()),
                    dueDate == null ? null : java.sql.Date.valueOf(dueDate.toString()));
        }
    }
}
