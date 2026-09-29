package net.officefloor.hq.app.note;

import java.time.Instant;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/notes — write a note against a target, stamped now so it sorts newest first. */
public class CreateNote {

    public void service(@RequestBody NewNote body, NoteRepository repository,
            ObjectResponse<Note> response) {
        String text = body.getText() == null ? "" : body.getText().trim();
        if (text.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A note requires text");
        }
        String targetType = body.getTargetType() == null ? "" : body.getTargetType().trim();
        if (targetType.isEmpty() || body.getTargetId() == null) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A note requires a target");
        }
        Note note = new Note();
        note.setTargetType(targetType);
        note.setTargetId(body.getTargetId());
        note.setText(text);
        note.setCreatedAt(Instant.now().toString());
        response.send(repository.save(note));
    }
}
