package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/projects/notes?projectId=&lt;id&gt; — a project's notes, newest first. */
public class NotesGetLogic {

    public void service(@HttpQueryParameter("projectId") String projectId,
            NoteRepository repository, ObjectResponse<List<Note>> response) {
        long id = projectId == null || projectId.isBlank() ? 0 : Long.parseLong(projectId.trim());
        response.send(repository.findByProject(id));
    }
}
