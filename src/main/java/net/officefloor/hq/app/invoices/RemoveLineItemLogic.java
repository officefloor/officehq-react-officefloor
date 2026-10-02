package net.officefloor.hq.app.invoices;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/invoices/{invoiceId}/line-items/{lineItemId}/remove — remove one thing being charged for
 * from an invoice and return the invoice's remaining line items (the invoice's derived total is
 * updated as a side effect).
 */
public class RemoveLineItemLogic {

    public void service(@HttpPathParameter("invoiceId") String invoiceId,
            @HttpPathParameter("lineItemId") String lineItemId, InvoiceService invoices,
            ObjectResponse<List<LineItemView>> response) {
        response.send(invoices.removeLineItem(Long.valueOf(invoiceId), Long.valueOf(lineItemId)));
    }
}
