package net.officefloor.hq.app;

import java.util.List;
import java.util.stream.Collectors;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/projects/{projectId}/tags — the tags attached to a project, by tag id order, each with
 * its name (a join over project_tag to tag) so the UI can show the chips.
 */
public class ListProjectTagsLogic {

    public void service(@HttpPathParameter("projectId") String projectId,
            ProjectTagRepository projectTags, TagRepository tags,
            ObjectResponse<List<TagView>> response) {
        Long id = Long.valueOf(projectId);
        List<TagView> views = projectTags.findByProjectIdOrderByTagIdAsc(id).stream()
                .map(link -> tags.findById(link.getTagId()).orElse(null))
                .filter(t -> t != null)
                .map(t -> new TagView(t.getId(), t.getName()))
                .collect(Collectors.toList());
        response.send(views);
    }
}
