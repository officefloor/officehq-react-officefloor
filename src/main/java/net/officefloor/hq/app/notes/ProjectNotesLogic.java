package net.officefloor.hq.app.notes;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/projects/{projectId}/notes — list a project's notes, newest first. */
public class ProjectNotesLogic {

    public void service(@HttpPathParameter("projectId") String projectId, NoteService notes,
            ObjectResponse<List<NoteView>> response) {
        response.send(notes.listForProject(Long.valueOf(projectId)));
    }
}
