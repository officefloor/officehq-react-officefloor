package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/projects/notes — write a note on a project; returns the notes afterwards, newest first. */
public class NotesPostLogic {

    public void service(@RequestBody NewNote body, NoteRepository repository,
            ObjectResponse<List<Note>> response) {
        if (body.getProjectId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A project id is required.");
        }
        if (body.getText() == null || body.getText().isBlank()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Note text is required.");
        }
        repository.create(body.getProjectId(), body.getText().trim());
        response.send(repository.findByProject(body.getProjectId()));
    }
}
