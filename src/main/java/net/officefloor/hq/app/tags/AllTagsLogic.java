package net.officefloor.hq.app.tags;

import java.util.List;
import net.officefloor.web.ObjectResponse;

/** GET /api/tags — list every tag so the UI can offer them as choices to add to a project. */
public class AllTagsLogic {

    public void service(TagService tags, ObjectResponse<List<TagView>> response) {
        response.send(tags.listAll());
    }
}
