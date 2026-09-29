package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/projects/tags — put a tag on a project; returns the project's tags afterwards. */
public class ProjectTagsPostLogic {

    public void service(@RequestBody TagAssignment body, TagRepository repository,
            ObjectResponse<List<Tag>> response) {
        if (body.getProjectId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A project id is required.");
        }
        if (body.getTagId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A tag id is required.");
        }
        if (repository.findById(body.getTagId()) == null) {
            throw new HttpException(HttpStatus.NOT_FOUND, "No such tag.");
        }
        repository.addToProject(body.getProjectId(), body.getTagId());
        response.send(repository.findByProject(body.getProjectId()));
    }
}
