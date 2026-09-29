package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/payments?invoiceId=&lt;id&gt; — list an invoice's payments. */
public class PaymentsGetLogic {

    public void service(@HttpQueryParameter("invoiceId") String invoiceId,
            PaymentRepository repository, ObjectResponse<List<Payment>> response) {
        long id = invoiceId == null || invoiceId.isBlank() ? 0 : Long.parseLong(invoiceId.trim());
        response.send(repository.findByInvoice(id));
    }
}
