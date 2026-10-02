package net.officefloor.hq.app.invoices;

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/invoices/{invoiceId} — one invoice, with its status worked out from the payments recorded
 * against it (PAID once covered, PARTIAL once part paid, otherwise its stored lifecycle status).
 */
public class InvoiceLogic {

    public void service(@HttpPathParameter("invoiceId") String invoiceId, InvoiceService invoices,
            ObjectResponse<InvoiceView> response) {
        response.send(invoices.get(Long.valueOf(invoiceId)));
    }
}
