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
        // project references client via FK; H2 refuses TRUNCATE on a referenced table, so drop
        // referential integrity for the duration of the clear, then restore it.
        jdbc.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbc.execute("TRUNCATE TABLE note RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE project_tag RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE tag RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE task RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE payment RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE line_item RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE invoice RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE project RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE contact RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE client RESTART IDENTITY");
        jdbc.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    /** Insert the fixture a spec needs; the payload shape evolves with the schema. */
    @PostMapping("/seed")
    @SuppressWarnings("unchecked")
    public void seed(@RequestBody Map<String, Object> fixture) {
        List<Map<String, Object>> clients =
                (List<Map<String, Object>>) fixture.getOrDefault("clients", List.of());
        for (Map<String, Object> client : clients) {
            jdbc.update("INSERT INTO client (id, name, email) VALUES (?, ?, ?)",
                    ((Number) client.get("id")).longValue(), client.get("name"), client.get("email"));
        }
        List<Map<String, Object>> projects =
                (List<Map<String, Object>>) fixture.getOrDefault("projects", List.of());
        for (Map<String, Object> project : projects) {
            Object status = project.getOrDefault("status", "ACTIVE");
            boolean archived = Boolean.TRUE.equals(project.getOrDefault("archived", Boolean.FALSE));
            jdbc.update(
                    "INSERT INTO project (id, name, client_id, status, archived) VALUES (?, ?, ?, ?, ?)",
                    ((Number) project.get("id")).longValue(), project.get("name"),
                    ((Number) project.get("clientId")).longValue(), status, archived);
        }
        List<Map<String, Object>> tasks =
                (List<Map<String, Object>>) fixture.getOrDefault("tasks", List.of());
        for (Map<String, Object> task : tasks) {
            Object done = task.getOrDefault("done", Boolean.FALSE);
            jdbc.update("INSERT INTO task (id, project_id, title, done) VALUES (?, ?, ?, ?)",
                    ((Number) task.get("id")).longValue(),
                    ((Number) task.get("projectId")).longValue(), task.get("title"),
                    Boolean.TRUE.equals(done));
        }
        List<Map<String, Object>> notes =
                (List<Map<String, Object>>) fixture.getOrDefault("notes", List.of());
        long maxNoteId = 0;
        for (Map<String, Object> note : notes) {
            long noteId = ((Number) note.get("id")).longValue();
            maxNoteId = Math.max(maxNoteId, noteId);
            jdbc.update(
                    "INSERT INTO note (id, target_type, target_id, body, at) VALUES (?, ?, ?, ?, ?)",
                    noteId, note.get("targetType"),
                    ((Number) note.get("targetId")).longValue(), note.get("text"),
                    java.sql.Timestamp.from(java.time.Instant.parse(note.get("at").toString())));
        }
        if (maxNoteId > 0) {
            // Seeding explicit ids does not advance H2's IDENTITY sequence, so a note created later
            // through the API would collide on id=1. Nudge the sequence past the seeded ids.
            jdbc.execute("ALTER TABLE note ALTER COLUMN id RESTART WITH " + (maxNoteId + 1));
        }
        List<Map<String, Object>> tags =
                (List<Map<String, Object>>) fixture.getOrDefault("tags", List.of());
        for (Map<String, Object> tag : tags) {
            jdbc.update("INSERT INTO tag (id, name) VALUES (?, ?)",
                    ((Number) tag.get("id")).longValue(), tag.get("name"));
        }
        List<Map<String, Object>> projectTags =
                (List<Map<String, Object>>) fixture.getOrDefault("projectTags", List.of());
        for (Map<String, Object> projectTag : projectTags) {
            jdbc.update("INSERT INTO project_tag (project_id, tag_id) VALUES (?, ?)",
                    ((Number) projectTag.get("projectId")).longValue(),
                    ((Number) projectTag.get("tagId")).longValue());
        }
        List<Map<String, Object>> contacts =
                (List<Map<String, Object>>) fixture.getOrDefault("contacts", List.of());
        for (Map<String, Object> contact : contacts) {
            jdbc.update(
                    "INSERT INTO contact (id, client_id, name, email, role) VALUES (?, ?, ?, ?, ?)",
                    ((Number) contact.get("id")).longValue(),
                    ((Number) contact.get("clientId")).longValue(), contact.get("name"),
                    contact.get("email"), contact.get("role"));
        }
        List<Map<String, Object>> invoices =
                (List<Map<String, Object>>) fixture.getOrDefault("invoices", List.of());
        for (Map<String, Object> invoice : invoices) {
            Object status = invoice.getOrDefault("status", "DRAFT");
            Object issuedDate = invoice.get("issuedDate");
            Object dueDate = invoice.get("dueDate");
            long invoiceId = ((Number) invoice.get("id")).longValue();
            // An invoice's amount is the sum of its line items (quantity times unit price); work it
            // out here so a fixture lists the things being charged for, not a pre-computed figure.
            // A fixture may still pass an explicit amount for an invoice with no line items.
            List<Map<String, Object>> lineItems =
                    (List<Map<String, Object>>) invoice.getOrDefault("lineItems", null);
            java.math.BigDecimal amount;
            if (lineItems != null) {
                amount = java.math.BigDecimal.ZERO;
                for (Map<String, Object> lineItem : lineItems) {
                    java.math.BigDecimal unitPrice =
                            new java.math.BigDecimal(lineItem.get("unitPrice").toString());
                    int qty = ((Number) lineItem.get("qty")).intValue();
                    amount = amount.add(unitPrice.multiply(java.math.BigDecimal.valueOf(qty)));
                }
            } else {
                lineItems = List.of();
                Object explicit = invoice.get("amount");
                amount = explicit == null ? java.math.BigDecimal.ZERO
                        : new java.math.BigDecimal(explicit.toString());
            }
            jdbc.update(
                    "INSERT INTO invoice (id, project_id, amount, status, issued_date, due_date) "
                            + "VALUES (?, ?, ?, ?, ?, ?)",
                    invoiceId, ((Number) invoice.get("projectId")).longValue(), amount, status,
                    issuedDate == null ? null : java.sql.Date.valueOf(issuedDate.toString()),
                    dueDate == null ? null : java.sql.Date.valueOf(dueDate.toString()));
            for (Map<String, Object> lineItem : lineItems) {
                jdbc.update(
                        "INSERT INTO line_item (id, invoice_id, description, quantity, unit_price) "
                                + "VALUES (?, ?, ?, ?, ?)",
                        ((Number) lineItem.get("id")).longValue(), invoiceId,
                        lineItem.get("description"),
                        ((Number) lineItem.get("qty")).intValue(),
                        new java.math.BigDecimal(lineItem.get("unitPrice").toString()));
            }
        }
        List<Map<String, Object>> payments =
                (List<Map<String, Object>>) fixture.getOrDefault("payments", List.of());
        long maxPaymentId = 0;
        for (Map<String, Object> payment : payments) {
            long paymentId = ((Number) payment.get("id")).longValue();
            maxPaymentId = Math.max(maxPaymentId, paymentId);
            jdbc.update(
                    "INSERT INTO payment (id, invoice_id, amount, paid_date) VALUES (?, ?, ?, ?)",
                    paymentId, ((Number) payment.get("invoiceId")).longValue(),
                    new java.math.BigDecimal(payment.get("amount").toString()),
                    java.sql.Date.valueOf(payment.get("date").toString()));
        }
        if (maxPaymentId > 0) {
            // Seeding explicit ids does not advance H2's IDENTITY sequence, so a payment created later
            // through the API would collide on id=1. Nudge the sequence past the seeded ids.
            jdbc.execute("ALTER TABLE payment ALTER COLUMN id RESTART WITH " + (maxPaymentId + 1));
        }
    }
}
