package net.officefloor.hq.app.notes;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** POST /api/invoices/{invoiceId}/notes — write the note from the JSON body on the invoice. */
public class AddInvoiceNoteLogic {

    public void service(@HttpPathParameter("invoiceId") String invoiceId, NewNote body,
            NoteService notes, ObjectResponse<List<NoteView>> response) {
        response.send(notes.addToInvoice(Long.valueOf(invoiceId), body.getText()));
    }
}
