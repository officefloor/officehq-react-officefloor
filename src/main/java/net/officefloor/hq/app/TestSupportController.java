package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.OffsetDateTime;
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
 * <p>{@code seed} is one helper per table so a new entity is a new {@code seedX} method plus one
 * call, not surgery on a growing monolith; {@code reset} is data-driven off {@link #DOMAIN_TABLES}.
 */
@Profile("harness")
@RestController
@RequestMapping("/__test__")
public class TestSupportController {

    /**
     * Every domain table, cleared on reset. Order is irrelevant: referential integrity is lifted
     * for the truncate and {@code RESTART IDENTITY} resets each table's generator. Add a table here
     * when its schema arrives.
     */
    private static final List<String> DOMAIN_TABLES = List.of(
            "notes", "project_tags", "tags", "tasks", "payments", "line_items",
            "invoices", "contacts", "projects", "clients");

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
            for (String table : DOMAIN_TABLES) {
                jdbc.execute("TRUNCATE TABLE " + table + " RESTART IDENTITY");
            }
        } finally {
            jdbc.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }

    /** Insert the fixture a spec needs; each table has its own {@code seedX} helper below. */
    @PostMapping("/seed")
    public void seed(@RequestBody Map<String, Object> fixture) {
        seedClients(fixture);
        seedProjects(fixture);
        seedContacts(fixture);
        seedTasks(fixture);
        seedTags(fixture);
        seedProjectTags(fixture);
        seedNotes(fixture);
        seedInvoices(fixture);
        seedPayments(fixture);
    }

    private void seedClients(Map<String, Object> fixture) {
        for (Map<String, Object> c : rows(fixture, "clients")) {
            jdbc.update("INSERT INTO clients (id, name, email) VALUES (?, ?, ?)",
                    id(c, "id"), c.get("name"), c.get("email"));
        }
    }

    private void seedProjects(Map<String, Object> fixture) {
        for (Map<String, Object> p : rows(fixture, "projects")) {
            jdbc.update("INSERT INTO projects (id, name, client_id) VALUES (?, ?, ?)",
                    id(p, "id"), p.get("name"), id(p, "clientId"));
        }
    }

    private void seedContacts(Map<String, Object> fixture) {
        for (Map<String, Object> ct : rows(fixture, "contacts")) {
            jdbc.update(
                    "INSERT INTO contacts (id, client_id, name, email, role) VALUES (?, ?, ?, ?, ?)",
                    id(ct, "id"), id(ct, "clientId"),
                    ct.get("name"), ct.get("email"), ct.get("role"));
        }
    }

    private void seedTasks(Map<String, Object> fixture) {
        for (Map<String, Object> t : rows(fixture, "tasks")) {
            Object done = t.get("done");
            jdbc.update("INSERT INTO tasks (id, project_id, title, done) VALUES (?, ?, ?, ?)",
                    id(t, "id"), id(t, "projectId"), t.get("title"),
                    done != null && Boolean.parseBoolean(done.toString()));
        }
    }

    private void seedTags(Map<String, Object> fixture) {
        for (Map<String, Object> tg : rows(fixture, "tags")) {
            jdbc.update("INSERT INTO tags (id, name) VALUES (?, ?)", id(tg, "id"), tg.get("name"));
        }
    }

    private void seedProjectTags(Map<String, Object> fixture) {
        for (Map<String, Object> pt : rows(fixture, "projectTags")) {
            jdbc.update("INSERT INTO project_tags (project_id, tag_id) VALUES (?, ?)",
                    id(pt, "projectId"), id(pt, "tagId"));
        }
    }

    private void seedNotes(Map<String, Object> fixture) {
        List<Map<String, Object>> notes = rows(fixture, "notes");
        for (Map<String, Object> n : notes) {
            Object at = n.get("at");
            jdbc.update(
                    "INSERT INTO notes (id, target_type, target_id, body, created_at)"
                            + " VALUES (?, ?, ?, ?, ?)",
                    id(n, "id"), n.get("targetType"), id(n, "targetId"), n.get("text"),
                    at == null ? null : OffsetDateTime.parse(at.toString()));
        }
        // notes are also inserted app-side (NoteRepository) via the IDENTITY generator, so advance
        // it past any seeded ids (see bumpIdentity).
        if (!notes.isEmpty()) {
            bumpIdentity("notes");
        }
    }

    private void seedInvoices(Map<String, Object> fixture) {
        for (Map<String, Object> inv : rows(fixture, "invoices")) {
            Object status = inv.get("status");
            Object issuedDate = inv.get("issuedDate");
            Object dueDate = inv.get("dueDate");
            // Amount is derived from the invoice's line items (V13), so it is not seeded directly;
            // the column defaults to 0.
            jdbc.update(
                    "INSERT INTO invoices (id, project_id, status, issued_date, due_date)"
                            + " VALUES (?, ?, ?, ?, ?)",
                    id(inv, "id"), id(inv, "projectId"),
                    status == null ? "DRAFT" : status.toString(),
                    issuedDate == null ? null : Date.valueOf(issuedDate.toString()),
                    dueDate == null ? null : Date.valueOf(dueDate.toString()));
            seedLineItems(inv);
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
                    new BigDecimal(li.get("unitPrice").toString()));
        }
    }

    private void seedPayments(Map<String, Object> fixture) {
        List<Map<String, Object>> payments = rows(fixture, "payments");
        for (Map<String, Object> p : payments) {
            jdbc.update(
                    "INSERT INTO payments (id, invoice_id, amount, paid_date) VALUES (?, ?, ?, ?)",
                    id(p, "id"), id(p, "invoiceId"),
                    new BigDecimal(p.get("amount").toString()),
                    Date.valueOf(p.get("date").toString()));
        }
        // payments are also inserted app-side (PaymentRepository) via the IDENTITY generator, so
        // advance it past any seeded ids (see bumpIdentity).
        if (!payments.isEmpty()) {
            bumpIdentity("payments");
        }
    }

    /** The list of fixture rows under {@code key}, or empty when the spec omitted that table. */
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> rows(Map<String, Object> fixture, String key) {
        return (List<Map<String, Object>>) fixture.getOrDefault(key, List.of());
    }

    /** Coerce a JSON number field (Integer/Long/Double from Jackson) to the {@code long} id. */
    private static long id(Map<String, Object> row, String key) {
        return ((Number) row.get(key)).longValue();
    }

    /**
     * Seeding explicit ids into an IDENTITY column does not advance H2's generator, so a later
     * app-side INSERT would reuse a seeded id and hit the PK. Bump the generator past the seeded
     * rows. Use for any table seeded with explicit ids that is also written to app-side.
     */
    private void bumpIdentity(String table) {
        Long next = jdbc.queryForObject(
                "SELECT COALESCE(MAX(id), 0) + 1 FROM " + table, Long.class);
        jdbc.execute("ALTER TABLE " + table + " ALTER COLUMN id RESTART WITH " + next);
    }
}
