package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/projects/tags?projectId=&lt;id&gt; — the tags currently on a project. */
public class ProjectTagsGetLogic {

    public void service(@HttpQueryParameter("projectId") String projectId,
            TagRepository repository, ObjectResponse<List<Tag>> response) {
        long id = projectId == null || projectId.isBlank() ? 0 : Long.parseLong(projectId.trim());
        response.send(repository.findByProject(id));
    }
}
