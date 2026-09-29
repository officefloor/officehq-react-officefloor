package net.officefloor.hq.app;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/** Data access for {@link Note} rows (schema in V16__notes.sql). */
@Repository
public class NoteRepository {

    private static final String PROJECT = "project";

    private final JdbcTemplate jdbc;

    public NoteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Note> MAPPER =
            (rs, i) -> new Note(rs.getLong("id"), rs.getString("body"));

    /** A project's notes, newest first (created_at descending, id as a stable tie-break). */
    public List<Note> findByProject(long projectId) {
        return jdbc.query(
                "SELECT id, body FROM notes WHERE target_type = ? AND target_id = ?"
                        + " ORDER BY created_at DESC, id DESC",
                MAPPER, PROJECT, projectId);
    }

    /** Write a new note on a project; stamped with the current time so it sorts to the top. */
    public void create(long projectId, String text) {
        jdbc.update("INSERT INTO notes (target_type, target_id, body) VALUES (?, ?, ?)",
                PROJECT, projectId, text);
    }
}
