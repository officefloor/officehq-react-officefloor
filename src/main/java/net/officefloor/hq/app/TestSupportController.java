package net.officefloor.hq.app;

import java.time.LocalDate;
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
        // Disable FK checks so a referenced parent (clients) can be truncated alongside its child.
        jdbc.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbc.execute("TRUNCATE TABLE payments RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE line_items RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE invoices RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE contacts RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE tasks RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE notes RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE project_tags RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE tags RESTART IDENTITY");
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
        long maxClientId = 0;
        for (Map<String, Object> client : clients) {
            long id = ((Number) client.get("id")).longValue();
            // Seed with the fixture's explicit id (JdbcTemplate, not JPA) so specs can assert by id.
            jdbc.update("INSERT INTO clients (id, name, email) VALUES (?, ?, ?)",
                    id, client.get("name"), client.get("email"));
            maxClientId = Math.max(maxClientId, id);
        }
        if (maxClientId > 0) {
            // Advance the identity so app-generated ids don't collide with seeded rows.
            jdbc.execute("ALTER TABLE clients ALTER COLUMN id RESTART WITH " + (maxClientId + 1));
        }

        List<Map<String, Object>> projects =
                (List<Map<String, Object>>) fixture.getOrDefault("projects", List.of());
        long maxProjectId = 0;
        for (Map<String, Object> project : projects) {
            long id = ((Number) project.get("id")).longValue();
            long clientId = ((Number) project.get("clientId")).longValue();
            String status = (String) project.getOrDefault("status", "ACTIVE");
            boolean archived = Boolean.TRUE.equals(project.getOrDefault("archived", Boolean.FALSE));
            jdbc.update(
                    "INSERT INTO projects (id, name, client_id, status, archived) VALUES (?, ?, ?, ?, ?)",
                    id, project.get("name"), clientId, status, archived);
            maxProjectId = Math.max(maxProjectId, id);
        }
        if (maxProjectId > 0) {
            jdbc.execute("ALTER TABLE projects ALTER COLUMN id RESTART WITH " + (maxProjectId + 1));
        }

        List<Map<String, Object>> contacts =
                (List<Map<String, Object>>) fixture.getOrDefault("contacts", List.of());
        long maxContactId = 0;
        for (Map<String, Object> contact : contacts) {
            long id = ((Number) contact.get("id")).longValue();
            long clientId = ((Number) contact.get("clientId")).longValue();
            jdbc.update("INSERT INTO contacts (id, client_id, name, email, role) VALUES (?, ?, ?, ?, ?)",
                    id, clientId, contact.get("name"), contact.get("email"), contact.get("role"));
            maxContactId = Math.max(maxContactId, id);
        }
        if (maxContactId > 0) {
            jdbc.execute("ALTER TABLE contacts ALTER COLUMN id RESTART WITH " + (maxContactId + 1));
        }

        List<Map<String, Object>> tasks =
                (List<Map<String, Object>>) fixture.getOrDefault("tasks", List.of());
        long maxTaskId = 0;
        for (Map<String, Object> task : tasks) {
            long id = ((Number) task.get("id")).longValue();
            long projectId = ((Number) task.get("projectId")).longValue();
            boolean done = Boolean.TRUE.equals(task.getOrDefault("done", Boolean.FALSE));
            jdbc.update("INSERT INTO tasks (id, project_id, title, done) VALUES (?, ?, ?, ?)",
                    id, projectId, task.get("title"), done);
            maxTaskId = Math.max(maxTaskId, id);
        }
        if (maxTaskId > 0) {
            jdbc.execute("ALTER TABLE tasks ALTER COLUMN id RESTART WITH " + (maxTaskId + 1));
        }

        List<Map<String, Object>> tags =
                (List<Map<String, Object>>) fixture.getOrDefault("tags", List.of());
        long maxTagId = 0;
        for (Map<String, Object> tag : tags) {
            long id = ((Number) tag.get("id")).longValue();
            jdbc.update("INSERT INTO tags (id, name) VALUES (?, ?)", id, tag.get("name"));
            maxTagId = Math.max(maxTagId, id);
        }
        if (maxTagId > 0) {
            jdbc.execute("ALTER TABLE tags ALTER COLUMN id RESTART WITH " + (maxTagId + 1));
        }

        List<Map<String, Object>> projectTags =
                (List<Map<String, Object>>) fixture.getOrDefault("projectTags", List.of());
        for (Map<String, Object> projectTag : projectTags) {
            long projectId = ((Number) projectTag.get("projectId")).longValue();
            long tagId = ((Number) projectTag.get("tagId")).longValue();
            jdbc.update("INSERT INTO project_tags (project_id, tag_id) VALUES (?, ?)",
                    projectId, tagId);
        }

        List<Map<String, Object>> notes =
                (List<Map<String, Object>>) fixture.getOrDefault("notes", List.of());
        long maxNoteId = 0;
        for (Map<String, Object> note : notes) {
            long id = ((Number) note.get("id")).longValue();
            long targetId = ((Number) note.get("targetId")).longValue();
            jdbc.update(
                    "INSERT INTO notes (id, target_type, target_id, text, at) VALUES (?, ?, ?, ?, ?)",
                    id, note.get("targetType"), targetId, note.get("text"),
                    OffsetDateTime.parse((String) note.get("at")));
            maxNoteId = Math.max(maxNoteId, id);
        }
        if (maxNoteId > 0) {
            jdbc.execute("ALTER TABLE notes ALTER COLUMN id RESTART WITH " + (maxNoteId + 1));
        }

        List<Map<String, Object>> invoices =
                (List<Map<String, Object>>) fixture.getOrDefault("invoices", List.of());
        long maxInvoiceId = 0;
        long maxLineItemId = 0;
        for (Map<String, Object> invoice : invoices) {
            long id = ((Number) invoice.get("id")).longValue();
            long projectId = ((Number) invoice.get("projectId")).longValue();
            // An invoice is itemised: its amount is derived as the sum of each line's qty * price.
            // A fixture may still pass an explicit amount for a line-item-less invoice.
            List<Map<String, Object>> lineItems =
                    (List<Map<String, Object>>) invoice.getOrDefault("lineItems", List.of());
            double amount = lineItems.isEmpty() && invoice.get("amount") != null
                    ? ((Number) invoice.get("amount")).doubleValue()
                    : 0;
            for (Map<String, Object> lineItem : lineItems) {
                amount += ((Number) lineItem.get("qty")).doubleValue()
                        * ((Number) lineItem.get("unitPrice")).doubleValue();
            }
            String status = (String) invoice.getOrDefault("status", "UNPAID");
            Object issuedDate = invoice.getOrDefault("issuedDate", LocalDate.now().toString());
            Object dueDate = invoice.getOrDefault("dueDate", LocalDate.now().toString());
            jdbc.update(
                    "INSERT INTO invoices (id, project_id, amount, status, issued_date, due_date) VALUES (?, ?, ?, ?, ?, ?)",
                    id, projectId, amount, status, issuedDate, dueDate);
            maxInvoiceId = Math.max(maxInvoiceId, id);
            for (Map<String, Object> lineItem : lineItems) {
                long lineItemId = ((Number) lineItem.get("id")).longValue();
                jdbc.update(
                        "INSERT INTO line_items (id, invoice_id, description, qty, unit_price) VALUES (?, ?, ?, ?, ?)",
                        lineItemId, id, lineItem.get("description"),
                        ((Number) lineItem.get("qty")).intValue(), lineItem.get("unitPrice"));
                maxLineItemId = Math.max(maxLineItemId, lineItemId);
            }
        }
        if (maxInvoiceId > 0) {
            jdbc.execute("ALTER TABLE invoices ALTER COLUMN id RESTART WITH " + (maxInvoiceId + 1));
        }
        if (maxLineItemId > 0) {
            jdbc.execute("ALTER TABLE line_items ALTER COLUMN id RESTART WITH " + (maxLineItemId + 1));
        }

        List<Map<String, Object>> payments =
                (List<Map<String, Object>>) fixture.getOrDefault("payments", List.of());
        long maxPaymentId = 0;
        for (Map<String, Object> payment : payments) {
            long id = ((Number) payment.get("id")).longValue();
            long invoiceId = ((Number) payment.get("invoiceId")).longValue();
            jdbc.update(
                    "INSERT INTO payments (id, invoice_id, amount, paid_date) VALUES (?, ?, ?, ?)",
                    id, invoiceId, ((Number) payment.get("amount")).doubleValue(),
                    payment.get("date"));
            maxPaymentId = Math.max(maxPaymentId, id);
        }
        if (maxPaymentId > 0) {
            jdbc.execute("ALTER TABLE payments ALTER COLUMN id RESTART WITH " + (maxPaymentId + 1));
        }
    }
}
