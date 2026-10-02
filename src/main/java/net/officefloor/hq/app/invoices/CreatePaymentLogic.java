package net.officefloor.hq.app.invoices;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/invoices/{invoiceId}/payments — record a payment a client has made against an invoice
 * and return the invoice's payments.
 */
public class CreatePaymentLogic {

    public void service(@HttpPathParameter("invoiceId") String invoiceId, NewPayment body,
            InvoiceService invoices, ObjectResponse<List<PaymentView>> response) {
        response.send(invoices.addPayment(Long.valueOf(invoiceId), body));
    }
}
