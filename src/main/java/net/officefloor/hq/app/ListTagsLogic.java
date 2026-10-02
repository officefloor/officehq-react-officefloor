package net.officefloor.hq.app;

import java.util.List;
import java.util.stream.Collectors;
import net.officefloor.web.ObjectResponse;

/** GET /api/tags — every tag, by name order, for the project's add-a-tag picker. */
public class ListTagsLogic {

    public void service(TagRepository tags, ObjectResponse<List<TagView>> response) {
        List<TagView> views = tags.findAllByOrderByNameAsc().stream()
                .map(t -> new TagView(t.getId(), t.getName()))
                .collect(Collectors.toList());
        response.send(views);
    }
}
