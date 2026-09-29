package net.officefloor.hq.app;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/** Data access for {@link Tag} rows and project_tags links (schema in V15__tags.sql). */
@Repository
public class TagRepository {

    private final JdbcTemplate jdbc;

    public TagRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Tag> MAPPER =
            (rs, i) -> new Tag(rs.getLong("id"), rs.getString("name"));

    /** Every tag in the catalogue (the options the owner can add to a project). */
    public List<Tag> findAll() {
        return jdbc.query("SELECT id, name FROM tags ORDER BY id", MAPPER);
    }

    /** The tags currently on a project, in a stable order. */
    public List<Tag> findByProject(long projectId) {
        return jdbc.query(
                "SELECT t.id, t.name FROM tags t"
                        + " JOIN project_tags pt ON pt.tag_id = t.id"
                        + " WHERE pt.project_id = ? ORDER BY t.id",
                MAPPER, projectId);
    }

    public Tag findById(long id) {
        List<Tag> found = jdbc.query("SELECT id, name FROM tags WHERE id = ?", MAPPER, id);
        return found.isEmpty() ? null : found.get(0);
    }

    /** Link a tag to a project; a repeated link is a no-op (idempotent). */
    public void addToProject(long projectId, long tagId) {
        jdbc.update(
                "MERGE INTO project_tags (project_id, tag_id) KEY (project_id, tag_id)"
                        + " VALUES (?, ?)",
                projectId, tagId);
    }

    /** Unlink a tag from a project. */
    public void removeFromProject(long projectId, long tagId) {
        jdbc.update("DELETE FROM project_tags WHERE project_id = ? AND tag_id = ?",
                projectId, tagId);
    }
}
