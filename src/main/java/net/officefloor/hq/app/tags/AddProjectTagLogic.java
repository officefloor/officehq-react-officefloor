package net.officefloor.hq.app.tags;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** POST /api/projects/{projectId}/tags — put the tag from the JSON body on the project. */
public class AddProjectTagLogic {

    public void service(@HttpPathParameter("projectId") String projectId, NewProjectTag body,
            TagService tags, ObjectResponse<List<TagView>> response) {
        response.send(tags.addToProject(Long.valueOf(projectId), body.getTagId()));
    }
}
