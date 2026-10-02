package net.officefloor.hq.app.invoices;

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** POST /api/invoices/{invoiceId}/send — send a draft invoice and return the updated row. */
public class SendInvoiceLogic {

    public void service(@HttpPathParameter("invoiceId") String invoiceId, InvoiceService invoices,
            ObjectResponse<InvoiceView> response) {
        response.send(invoices.send(Long.valueOf(invoiceId)));
    }
}
