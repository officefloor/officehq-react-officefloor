package net.officefloor.hq.app.tag;

import java.util.List;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/** GET /api/tags — every tag that exists, so a project can pick one to attach. */
public class ListTags {

    public void service(JdbcTemplate jdbc, ObjectResponse<List<TagView>> response) {
        List<TagView> tags = jdbc.query("SELECT id, name FROM tags ORDER BY id",
                (rs, i) -> new TagView(rs.getLong("id"), rs.getString("name")));
        response.send(tags);
    }
}
