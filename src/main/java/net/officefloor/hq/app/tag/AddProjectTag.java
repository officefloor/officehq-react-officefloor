package net.officefloor.hq.app.tag;

import net.officefloor.hq.app.Audit;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/projects/{id}/tags — attach a tag to a project so it can be grouped. Idempotent: adding
 * a tag the project already has is a no-op. Records the fact to the audit file.
 */
public class AddProjectTag {

    public void service(@HttpPathParameter("id") String id, @RequestBody NewProjectTag body,
            JdbcTemplate jdbc, Audit audit, ObjectResponse<TagView> response) {
        Long projectId = Long.valueOf(id);
        if (body.getTagId() == null) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A tag is required");
        }
        Long tagId = body.getTagId();
        String name;
        try {
            name = jdbc.queryForObject("SELECT name FROM tags WHERE id = ?", String.class, tagId);
        } catch (EmptyResultDataAccessException e) {
            throw new HttpException(HttpStatus.NOT_FOUND, "No such tag");
        }
        Integer already = jdbc.queryForObject(
                "SELECT COUNT(*) FROM project_tags WHERE project_id = ? AND tag_id = ?",
                Integer.class, projectId, tagId);
        if (already == null || already == 0) {
            jdbc.update("INSERT INTO project_tags (project_id, tag_id) VALUES (?, ?)", projectId,
                    tagId);
            audit.record("PROJECT_TAG_ADDED project=" + projectId + " tag=" + tagId);
        }
        response.send(new TagView(tagId, name));
    }
}
