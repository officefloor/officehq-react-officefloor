package net.officefloor.hq.app;

import java.util.List;
import java.util.stream.Collectors;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/invoices/{invoiceId}/notes — an invoice's notes, newest first, so the latest note is at
 * the top of the list. Notes are the generic {@code note} rows with target_type "invoice".
 */
public class ListInvoiceNotesLogic {

    public void service(@HttpPathParameter("invoiceId") String invoiceId, NoteRepository notes,
            ObjectResponse<List<NoteView>> response) {
        Long id = Long.valueOf(invoiceId);
        List<NoteView> views = notes.findByTargetTypeAndTargetIdOrderByAtDescIdDesc("invoice", id)
                .stream()
                .map(n -> new NoteView(n.getId(), n.getTargetType(), n.getTargetId(), n.getText(),
                        n.getAt().toString()))
                .collect(Collectors.toList());
        response.send(views);
    }
}
