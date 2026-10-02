package net.officefloor.hq.app.notes;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** POST /api/projects/{projectId}/notes — write the note from the JSON body on the project. */
public class AddProjectNoteLogic {

    public void service(@HttpPathParameter("projectId") String projectId, NewNote body,
            NoteService notes, ObjectResponse<List<NoteView>> response) {
        response.send(notes.addToProject(Long.valueOf(projectId), body.getText()));
    }
}
