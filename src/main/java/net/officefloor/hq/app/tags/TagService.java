package net.officefloor.hq.app.tags;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for the labels a user groups projects by. Tags are reusable labels; a project
 * carries many tags through the {@code project_tags} join table. The feature stays self-contained
 * and reads the join via SQL rather than importing the projects feature's Java types.
 */
@Service
public class TagService {

    private static final RowMapper<TagView> MAPPER =
            (rs, i) -> new TagView(rs.getLong("id"), rs.getString("name"));

    private final JdbcTemplate jdbc;

    public TagService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Every tag that exists, so the UI can offer them as choices. */
    @Transactional(readOnly = true)
    public List<TagView> listAll() {
        return jdbc.query("SELECT id, name FROM tags ORDER BY id ASC", MAPPER);
    }

    /** The tags currently on a project, in a stable order. */
    @Transactional(readOnly = true)
    public List<TagView> listForProject(Long projectId) {
        return jdbc.query(
                "SELECT t.id, t.name FROM tags t "
                        + "JOIN project_tags pt ON pt.tag_id = t.id "
                        + "WHERE pt.project_id = ? ORDER BY t.id ASC",
                MAPPER, projectId);
    }

    /** Put a tag on a project (idempotent) and return the project's resulting tags. */
    @Transactional
    public List<TagView> addToProject(Long projectId, Long tagId) {
        if (projectId == null || tagId == null) {
            throw new IllegalArgumentException("A project id and a tag id are required");
        }
        Integer existing = jdbc.queryForObject(
                "SELECT COUNT(*) FROM project_tags WHERE project_id = ? AND tag_id = ?",
                Integer.class, projectId, tagId);
        if (existing == null || existing == 0) {
            jdbc.update("INSERT INTO project_tags (project_id, tag_id) VALUES (?, ?)",
                    projectId, tagId);
        }
        return listForProject(projectId);
    }

    /** Take a tag off a project and return the project's remaining tags. */
    @Transactional
    public List<TagView> removeFromProject(Long projectId, Long tagId) {
        if (projectId == null || tagId == null) {
            throw new IllegalArgumentException("A project id and a tag id are required");
        }
        jdbc.update("DELETE FROM project_tags WHERE project_id = ? AND tag_id = ?",
                projectId, tagId);
        return listForProject(projectId);
    }
}
