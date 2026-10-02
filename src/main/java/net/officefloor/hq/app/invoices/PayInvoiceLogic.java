package net.officefloor.hq.app.invoices;

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** POST /api/invoices/{invoiceId}/pay — mark an invoice paid and return the updated row. */
public class PayInvoiceLogic {

    public void service(@HttpPathParameter("invoiceId") String invoiceId, InvoiceService invoices,
            ObjectResponse<InvoiceView> response) {
        response.send(invoices.pay(Long.valueOf(invoiceId)));
    }
}
