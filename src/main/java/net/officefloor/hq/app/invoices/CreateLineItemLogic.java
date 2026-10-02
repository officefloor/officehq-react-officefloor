package net.officefloor.hq.app.invoices;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/invoices/{invoiceId}/line-items — add one thing being charged for to an invoice and
 * return the invoice's line items (the invoice's derived total is updated as a side effect).
 */
public class CreateLineItemLogic {

    public void service(@HttpPathParameter("invoiceId") String invoiceId, NewLineItem body,
            InvoiceService invoices, ObjectResponse<List<LineItemView>> response) {
        response.send(invoices.addLineItem(Long.valueOf(invoiceId), body));
    }
}
