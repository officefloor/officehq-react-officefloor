package net.officefloor.hq.app.tags;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** POST /api/projects/{projectId}/tags/{tagId}/remove — take a tag off a project. */
public class RemoveProjectTagLogic {

    public void service(@HttpPathParameter("projectId") String projectId,
            @HttpPathParameter("tagId") String tagId, TagService tags,
            ObjectResponse<List<TagView>> response) {
        response.send(tags.removeFromProject(Long.valueOf(projectId), Long.valueOf(tagId)));
    }
}
