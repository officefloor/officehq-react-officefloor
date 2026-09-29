package net.officefloor.hq.app;

import java.util.ArrayList;
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
 *
 * <p>{@link #seed} is a plain sequence of one {@code seed*} method per entity. Teaching a new entity
 * to the harness is therefore additive: add its {@code seed*} method, call it from {@link #seed},
 * and add its {@code TRUNCATE} to {@link #reset} — no existing entity's code is touched.
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
        // Referential integrity is dropped for the duration so the tables can be truncated (and
        // their identity counters restarted) regardless of FK order (e.g. projects -> clients).
        jdbc.execute("SET REFERENTIAL_INTEGRITY FALSE");
        for (String table : List.of("notes", "project_tags", "tags", "tasks", "payments",
                "line_items", "invoices", "contacts", "projects", "clients")) {
            jdbc.execute("TRUNCATE TABLE " + table + " RESTART IDENTITY");
        }
        jdbc.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    /** Insert the fixture a spec needs; each entity is seeded by its own {@code seed*} method. */
    @PostMapping("/seed")
    public void seed(@RequestBody Map<String, Object> fixture) {
        seedClients(fixture);
        seedProjects(fixture);
        seedContacts(fixture);
        seedInvoices(fixture);
        seedPayments(fixture);
        seedTasks(fixture);
        seedTags(fixture);
        seedNotes(fixture);
        seedProjectTags(fixture);
    }

    private void seedClients(Map<String, Object> fixture) {
        for (Map<String, Object> c : rows(fixture, "clients")) {
            jdbc.update("INSERT INTO clients (id, name, email) VALUES (?, ?, ?)",
                    id(c, "id"), c.get("name"), c.get("email"));
        }
    }

    private void seedProjects(Map<String, Object> fixture) {
        for (Map<String, Object> p : rows(fixture, "projects")) {
            // status defaults to ACTIVE so a fixture that omits it gets an active project; archived
            // defaults to FALSE (the table default) unless the fixture pins it.
            new Insert("projects")
                    .set("id", id(p, "id"))
                    .set("name", p.get("name"))
                    .set("client_id", id(p, "clientId"))
                    .set("status", p.get("status") == null ? "ACTIVE" : p.get("status").toString())
                    .setIfPresent("archived", p.get("archived"))
                    .setIfPresent("budget", asDouble(p.get("budget")))
                    .run();
        }
    }

    private void seedContacts(Map<String, Object> fixture) {
        for (Map<String, Object> ct : rows(fixture, "contacts")) {
            jdbc.update(
                    "INSERT INTO contacts (id, client_id, name, email, role) VALUES (?, ?, ?, ?, ?)",
                    id(ct, "id"), id(ct, "clientId"), ct.get("name"), ct.get("email"),
                    ct.get("role"));
        }
    }

    private void seedInvoices(Map<String, Object> fixture) {
        for (Map<String, Object> in : rows(fixture, "invoices")) {
            // Insert only the columns the fixture supplies, so the table's own defaults apply when a
            // fixture omits them (a plain null would trip the NOT NULL columns). status defaults to
            // DRAFT and amount defaults to 0 — an invoice's amount is derived from its line items,
            // so fixtures list lineItems rather than a single typed amount.
            new Insert("invoices")
                    .set("id", id(in, "id"))
                    .set("project_id", id(in, "projectId"))
                    .set("status", in.get("status") == null ? "DRAFT" : in.get("status").toString())
                    .setIfPresent("amount", asDouble(in.get("amount")))
                    .setIfPresent("issued_date", asText(in.get("issuedDate")))
                    .setIfPresent("due_date", asText(in.get("dueDate")))
                    .run();
            seedLineItems(in);
        }
    }

    private void seedLineItems(Map<String, Object> invoice) {
        long invoiceId = id(invoice, "id");
        for (Map<String, Object> li : rows(invoice, "lineItems")) {
            jdbc.update(
                    "INSERT INTO line_items (id, invoice_id, description, qty, unit_price)"
                            + " VALUES (?, ?, ?, ?, ?)",
                    id(li, "id"), invoiceId, li.get("description"),
                    ((Number) li.get("qty")).intValue(),
                    ((Number) li.get("unitPrice")).doubleValue());
        }
    }

    private void seedPayments(Map<String, Object> fixture) {
        List<Map<String, Object>> payments = rows(fixture, "payments");
        for (Map<String, Object> p : payments) {
            jdbc.update(
                    "INSERT INTO payments (id, invoice_id, amount, paid_date) VALUES (?, ?, ?, ?)",
                    id(p, "id"), id(p, "invoiceId"), asDouble(p.get("amount")),
                    asText(p.get("date")));
        }
        if (!payments.isEmpty()) {
            // Seeding explicit ids does not advance H2's identity counter, so bump it past the
            // seeded rows or the app's next generated payment id would collide with a fixture id.
            Long next = jdbc.queryForObject("SELECT MAX(id) + 1 FROM payments", Long.class);
            jdbc.execute("ALTER TABLE payments ALTER COLUMN id RESTART WITH " + next);
        }
    }

    private void seedTasks(Map<String, Object> fixture) {
        for (Map<String, Object> t : rows(fixture, "tasks")) {
            jdbc.update("INSERT INTO tasks (id, project_id, title, done) VALUES (?, ?, ?, ?)",
                    id(t, "id"), id(t, "projectId"), t.get("title"),
                    Boolean.TRUE.equals(t.get("done")));
        }
    }

    private void seedTags(Map<String, Object> fixture) {
        for (Map<String, Object> tg : rows(fixture, "tags")) {
            jdbc.update("INSERT INTO tags (id, name) VALUES (?, ?)", id(tg, "id"), tg.get("name"));
        }
    }

    private void seedNotes(Map<String, Object> fixture) {
        List<Map<String, Object>> notes = rows(fixture, "notes");
        for (Map<String, Object> n : notes) {
            jdbc.update(
                    "INSERT INTO notes (id, target_type, target_id, text, created_at)"
                            + " VALUES (?, ?, ?, ?, ?)",
                    id(n, "id"), n.get("targetType"), id(n, "targetId"), n.get("text"),
                    n.get("at").toString());
        }
        if (!notes.isEmpty()) {
            // Seeding explicit ids does not advance H2's identity counter, so bump it past the
            // seeded rows or the app's next generated note id would collide with a fixture id.
            Long next = jdbc.queryForObject("SELECT MAX(id) + 1 FROM notes", Long.class);
            jdbc.execute("ALTER TABLE notes ALTER COLUMN id RESTART WITH " + next);
        }
    }

    private void seedProjectTags(Map<String, Object> fixture) {
        for (Map<String, Object> pt : rows(fixture, "projectTags")) {
            jdbc.update("INSERT INTO project_tags (project_id, tag_id) VALUES (?, ?)",
                    id(pt, "projectId"), id(pt, "tagId"));
        }
    }

    /** The list of fixture rows under {@code key}, or an empty list when the fixture omits it. */
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> rows(Map<String, Object> fixture, String key) {
        return (List<Map<String, Object>>) fixture.getOrDefault(key, List.of());
    }

    /** A fixture id column: JSON numbers arrive as {@link Number}, the tables use {@code BIGINT}. */
    private static long id(Map<String, Object> row, String key) {
        return ((Number) row.get(key)).longValue();
    }

    private static Double asDouble(Object value) {
        return value == null ? null : ((Number) value).doubleValue();
    }

    private static String asText(Object value) {
        return value == null ? null : value.toString();
    }

    /**
     * Builds an {@code INSERT} whose column set is decided per row, so a fixture can omit a column
     * and let the table's own default apply (a bare null would trip a NOT NULL column). Required
     * columns are added with {@link #set}; optional ones with {@link #setIfPresent}.
     */
    private final class Insert {
        private final String table;
        private final StringBuilder cols = new StringBuilder();
        private final StringBuilder marks = new StringBuilder();
        private final List<Object> args = new ArrayList<>();

        Insert(String table) {
            this.table = table;
        }

        Insert set(String column, Object value) {
            if (cols.length() > 0) {
                cols.append(", ");
                marks.append(", ");
            }
            cols.append(column);
            marks.append("?");
            args.add(value);
            return this;
        }

        Insert setIfPresent(String column, Object value) {
            return value == null ? this : set(column, value);
        }

        void run() {
            jdbc.update("INSERT INTO " + table + " (" + cols + ") VALUES (" + marks + ")",
                    args.toArray());
        }
    }
}
