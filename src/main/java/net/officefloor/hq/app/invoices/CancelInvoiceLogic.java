package net.officefloor.hq.app.invoices;

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** POST /api/invoices/{invoiceId}/cancel — void a sent invoice and return the updated row. */
public class CancelInvoiceLogic {

    public void service(@HttpPathParameter("invoiceId") String invoiceId, InvoiceService invoices,
            ObjectResponse<InvoiceView> response) {
        response.send(invoices.cancel(Long.valueOf(invoiceId)));
    }
}
