package net.officefloor.hq.app;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/invoices/get?id=&lt;id&gt; — fetch a single invoice by id, so a view showing one invoice
 * (e.g. the invoice detail) can read its derived status (SENT/PARTIAL/PAID) after recording a
 * payment, without re-listing the whole project.
 */
public class InvoiceGetLogic {

    public void service(@HttpQueryParameter("id") String id, InvoiceRepository repository,
            ObjectResponse<Invoice> response) {
        long invoiceId = id == null || id.isBlank() ? 0 : Long.parseLong(id.trim());
        Invoice invoice = repository.findById(invoiceId);
        if (invoice == null) {
            throw new HttpException(HttpStatus.NOT_FOUND, "No such invoice.");
        }
        response.send(invoice);
    }
}
