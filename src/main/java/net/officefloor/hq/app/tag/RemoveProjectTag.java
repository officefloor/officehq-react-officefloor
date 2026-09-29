package net.officefloor.hq.app.tag;

import net.officefloor.hq.app.Audit;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * POST /api/projects/{id}/tags/{tagId}/remove — detach a tag from a project. Records the fact to the
 * audit file. Returns which pairing was removed.
 */
public class RemoveProjectTag {

    public void service(@HttpPathParameter("id") String id,
            @HttpPathParameter("tagId") String tagId, JdbcTemplate jdbc, Audit audit,
            ObjectResponse<TagView> response) {
        Long projectId = Long.valueOf(id);
        Long tag = Long.valueOf(tagId);
        int removed = jdbc.update(
                "DELETE FROM project_tags WHERE project_id = ? AND tag_id = ?", projectId, tag);
        if (removed > 0) {
            audit.record("PROJECT_TAG_REMOVED project=" + projectId + " tag=" + tag);
        }
        response.send(new TagView(tag, null));
    }
}
