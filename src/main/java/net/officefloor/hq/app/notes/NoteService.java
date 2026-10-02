package net.officefloor.hq.app.notes;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for the free-text notes a user writes about a target (e.g. a project). Notes are
 * generic — a target is a (type, id) pair — and the feature stays self-contained, reaching the rows
 * through SQL rather than importing another feature's Java types. Notes come back newest first.
 */
@Service
public class NoteService {

    private static final RowMapper<NoteView> MAPPER = (rs, i) -> new NoteView(
            rs.getLong("id"), rs.getString("text"),
            rs.getObject("at", OffsetDateTime.class).toInstant().toString());

    private final JdbcTemplate jdbc;

    public NoteService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** The notes on a project, newest written first (ties broken by newest id). */
    @Transactional(readOnly = true)
    public List<NoteView> listForProject(Long projectId) {
        return jdbc.query(
                "SELECT id, text, at FROM notes "
                        + "WHERE target_type = 'project' AND target_id = ? "
                        + "ORDER BY at DESC, id DESC",
                MAPPER, projectId);
    }

    /** Write a note on a project (stamped now) and return the project's notes, newest first. */
    @Transactional
    public List<NoteView> addToProject(Long projectId, String text) {
        if (projectId == null || text == null || text.isBlank()) {
            throw new IllegalArgumentException("A project id and note text are required");
        }
        jdbc.update(
                "INSERT INTO notes (target_type, target_id, text, at) VALUES ('project', ?, ?, ?)",
                projectId, text, OffsetDateTime.now());
        return listForProject(projectId);
    }

    /** The notes on an invoice, newest written first (ties broken by newest id). */
    @Transactional(readOnly = true)
    public List<NoteView> listForInvoice(Long invoiceId) {
        return jdbc.query(
                "SELECT id, text, at FROM notes "
                        + "WHERE target_type = 'invoice' AND target_id = ? "
                        + "ORDER BY at DESC, id DESC",
                MAPPER, invoiceId);
    }

    /** Write a note on an invoice (stamped now) and return the invoice's notes, newest first. */
    @Transactional
    public List<NoteView> addToInvoice(Long invoiceId, String text) {
        if (invoiceId == null || text == null || text.isBlank()) {
            throw new IllegalArgumentException("An invoice id and note text are required");
        }
        jdbc.update(
                "INSERT INTO notes (target_type, target_id, text, at) VALUES ('invoice', ?, ?, ?)",
                invoiceId, text, OffsetDateTime.now());
        return listForInvoice(invoiceId);
    }
}
