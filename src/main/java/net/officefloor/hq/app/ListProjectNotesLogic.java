package net.officefloor.hq.app;

import java.util.List;
import java.util.stream.Collectors;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/projects/{projectId}/notes — a project's notes, newest first, so the latest note is at the
 * top of the list.
 */
public class ListProjectNotesLogic {

    public void service(@HttpPathParameter("projectId") String projectId, NoteRepository notes,
            ObjectResponse<List<NoteView>> response) {
        Long id = Long.valueOf(projectId);
        List<NoteView> views = notes.findByTargetTypeAndTargetIdOrderByAtDescIdDesc("project", id)
                .stream()
                .map(n -> new NoteView(n.getId(), n.getTargetType(), n.getTargetId(), n.getText(),
                        n.getAt().toString()))
                .collect(Collectors.toList());
        response.send(views);
    }
}
