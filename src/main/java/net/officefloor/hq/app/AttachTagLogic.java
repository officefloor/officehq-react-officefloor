package net.officefloor.hq.app;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/projects/{projectId}/tags — attach an existing tag to a project and echo back the tag.
 * An unknown project, a missing/unknown tag id, is rejected with 400 and nothing is written;
 * re-attaching a tag the project already carries is a no-op that still echoes the tag.
 */
public class AttachTagLogic {

    public void service(@HttpPathParameter("projectId") String projectId,
            @RequestBody NewProjectTag newProjectTag, ProjectRepository projects, TagRepository tags,
            ProjectTagRepository projectTags, Audit audit, ObjectResponse<TagView> response) {
        Long id = Long.valueOf(projectId);
        projects.findById(id).orElseThrow(
                () -> new HttpException(HttpStatus.BAD_REQUEST, "An existing project is required"));
        Long tagId = newProjectTag.getTagId();
        if (tagId == null) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A tag is required");
        }
        Tag tag = tags.findById(tagId).orElseThrow(
                () -> new HttpException(HttpStatus.BAD_REQUEST, "An existing tag is required"));
        if (!projectTags.existsByProjectIdAndTagId(id, tagId)) {
            projectTags.save(new ProjectTag(id, tagId));
            audit.record("PROJECT_TAG_ADDED projectId=" + id + " tagId=" + tagId);
        }
        response.send(new TagView(tag.getId(), tag.getName()));
    }
}
