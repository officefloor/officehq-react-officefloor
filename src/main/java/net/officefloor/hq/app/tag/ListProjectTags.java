package net.officefloor.hq.app.tag;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/** GET /api/projects/{id}/tags — the tags attached to one project, oldest first. */
public class ListProjectTags {

    public void service(@HttpPathParameter("id") String id, JdbcTemplate jdbc,
            ObjectResponse<List<TagView>> response) {
        List<TagView> tags = jdbc.query(
                "SELECT t.id, t.name FROM tags t "
                        + "JOIN project_tags pt ON pt.tag_id = t.id "
                        + "WHERE pt.project_id = ? ORDER BY t.id",
                (rs, i) -> new TagView(rs.getLong("id"), rs.getString("name")),
                Long.valueOf(id));
        response.send(tags);
    }
}
