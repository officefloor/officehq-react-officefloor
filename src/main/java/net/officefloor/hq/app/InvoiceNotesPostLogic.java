package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/invoices/notes — write a note on an invoice; returns the notes afterwards, newest first. */
public class InvoiceNotesPostLogic {

    public void service(@RequestBody NewInvoiceNote body, NoteRepository repository,
            ObjectResponse<List<Note>> response) {
        if (body.getInvoiceId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "An invoice id is required.");
        }
        if (body.getText() == null || body.getText().isBlank()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Note text is required.");
        }
        repository.createForInvoice(body.getInvoiceId(), body.getText().trim());
        response.send(repository.findByInvoice(body.getInvoiceId()));
    }
}
