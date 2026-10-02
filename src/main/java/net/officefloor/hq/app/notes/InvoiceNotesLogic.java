package net.officefloor.hq.app.notes;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/invoices/{invoiceId}/notes — list an invoice's notes, newest first. */
public class InvoiceNotesLogic {

    public void service(@HttpPathParameter("invoiceId") String invoiceId, NoteService notes,
            ObjectResponse<List<NoteView>> response) {
        response.send(notes.listForInvoice(Long.valueOf(invoiceId)));
    }
}
