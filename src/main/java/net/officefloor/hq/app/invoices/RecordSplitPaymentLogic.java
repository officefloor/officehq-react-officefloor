package net.officefloor.hq.app.invoices;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/clients/{clientId}/payments — record one lump payment a client made and split it across
 * several of their invoices, returning the payments created (one per invoice that got a share).
 */
public class RecordSplitPaymentLogic {

    public void service(@HttpPathParameter("clientId") String clientId, SplitPayment body,
            InvoiceService invoices, ObjectResponse<List<PaymentView>> response) {
        response.send(invoices.recordSplitPayment(body.getDate(), body.getAllocations()));
    }
}
