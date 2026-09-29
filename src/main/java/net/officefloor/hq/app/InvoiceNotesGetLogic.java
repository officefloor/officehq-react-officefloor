package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/invoices/notes?invoiceId=&lt;id&gt; — an invoice's notes, newest first. */
public class InvoiceNotesGetLogic {

    public void service(@HttpQueryParameter("invoiceId") String invoiceId,
            NoteRepository repository, ObjectResponse<List<Note>> response) {
        long id = invoiceId == null || invoiceId.isBlank() ? 0 : Long.parseLong(invoiceId.trim());
        response.send(repository.findByInvoice(id));
    }
}
