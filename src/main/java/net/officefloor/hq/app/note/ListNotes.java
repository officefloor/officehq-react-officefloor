package net.officefloor.hq.app.note;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * GET /api/notes?targetType=&lt;type&gt;&amp;targetId=&lt;id&gt; — every note on one target, NEWEST
 * FIRST (created_at descending, id as a stable tie-break).
 */
public class ListNotes {

    public void service(@HttpQueryParameter("targetType") String targetType,
            @HttpQueryParameter("targetId") String targetId, JdbcTemplate jdbc,
            ObjectResponse<List<NoteView>> response) {
        List<NoteView> notes = jdbc.query(
                "SELECT id, target_type, target_id, text, created_at FROM notes"
                        + " WHERE target_type = ? AND target_id = ?"
                        + " ORDER BY created_at DESC, id DESC",
                (rs, i) -> new NoteView(rs.getLong("id"), rs.getString("target_type"),
                        rs.getLong("target_id"), rs.getString("text"), rs.getString("created_at")),
                targetType, Long.valueOf(targetId));
        response.send(notes);
    }
}
