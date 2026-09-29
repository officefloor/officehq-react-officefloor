package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.ObjectResponse;

/** GET /api/tags — the whole tag catalogue (the options the owner can add to a project). */
public class TagsGetLogic {

    public void service(TagRepository repository, ObjectResponse<List<Tag>> response) {
        response.send(repository.findAll());
    }
}
