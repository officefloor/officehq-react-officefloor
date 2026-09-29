package net.officefloor.hq.app.tag;

import java.util.List;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * GET /api/project-tags — every project-to-tag attachment across all projects, so the projects list
 * can be filtered by a chosen label without asking each project for its tags one by one.
 */
public class ListProjectTagLinks {

    public void service(JdbcTemplate jdbc, ObjectResponse<List<ProjectTagLink>> response) {
        List<ProjectTagLink> links = jdbc.query(
                "SELECT project_id, tag_id FROM project_tags ORDER BY project_id, tag_id",
                (rs, i) -> new ProjectTagLink(rs.getLong("project_id"), rs.getLong("tag_id")));
        response.send(links);
    }
}
