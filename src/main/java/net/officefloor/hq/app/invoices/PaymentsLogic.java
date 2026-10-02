package net.officefloor.hq.app.invoices;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/invoices/{invoiceId}/payments — list the payments a client has made on an invoice. */
public class PaymentsLogic {

    public void service(@HttpPathParameter("invoiceId") String invoiceId, InvoiceService invoices,
            ObjectResponse<List<PaymentView>> response) {
        response.send(invoices.listPayments(Long.valueOf(invoiceId)));
    }
}
