package net.officefloor.hq.app;

import java.time.Instant;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/invoices/{invoiceId}/notes — write a note against an invoice, stamped with the current
 * time so it sorts to the top of the invoice's notes. A missing/blank text is rejected with 400 and
 * nothing is written.
 */
public class CreateInvoiceNoteLogic {

    public void service(@HttpPathParameter("invoiceId") String invoiceId,
            @RequestBody NewNote newNote, NoteRepository notes, ObjectResponse<NoteView> response) {
        Long id = Long.valueOf(invoiceId);
        String text = newNote.getText();
        if (text == null || text.isBlank()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A note text is required");
        }
        Note saved = notes.save(new Note("invoice", id, text.trim(), Instant.now()));
        response.send(new NoteView(saved.getId(), saved.getTargetType(), saved.getTargetId(),
                saved.getText(), saved.getAt().toString()));
    }
}
