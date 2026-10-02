package net.officefloor.hq.app.tags;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/projects/{projectId}/tags — list the tags currently on a project. */
public class ProjectTagsLogic {

    public void service(@HttpPathParameter("projectId") String projectId, TagService tags,
            ObjectResponse<List<TagView>> response) {
        response.send(tags.listForProject(Long.valueOf(projectId)));
    }
}
