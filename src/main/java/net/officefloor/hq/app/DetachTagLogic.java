package net.officefloor.hq.app;

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * DELETE /api/projects/{projectId}/tags/{tagId} — remove a tag from a project and echo back how many
 * tags the project has left. Removing a tag the project does not carry is a harmless no-op.
 */
public class DetachTagLogic {

    public void service(@HttpPathParameter("projectId") String projectId,
            @HttpPathParameter("tagId") String tagId, ProjectTagRepository projectTags, Audit audit,
            ObjectResponse<DetachTagView> response) {
        Long id = Long.valueOf(projectId);
        Long tag = Long.valueOf(tagId);
        for (ProjectTag link : projectTags.findByProjectIdAndTagId(id, tag)) {
            projectTags.deleteById(link.getId());
            audit.record("PROJECT_TAG_REMOVED projectId=" + id + " tagId=" + tag);
        }
        response.send(new DetachTagView(projectTags.findByProjectIdOrderByTagIdAsc(id).size()));
    }
}
