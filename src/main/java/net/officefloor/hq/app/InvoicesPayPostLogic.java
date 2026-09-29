package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/invoices/pay — mark an invoice paid. Flips its status to PAID and records one audit
 * line (INVOICE_PAID id=&lt;id&gt; amount=&lt;amount&gt;) so the action can be checked back later.
 */
public class InvoicesPayPostLogic {

    public void service(@RequestBody PayInvoice body, InvoiceRepository repository, Audit audit,
            ObjectResponse<Invoice> response) {
        if (body.getId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "An invoice id is required.");
        }
        Invoice invoice = repository.markPaid(body.getId());
        if (invoice == null) {
            throw new HttpException(HttpStatus.NOT_FOUND, "No such invoice.");
        }
        BigDecimal amount = invoice.amount().setScale(2, RoundingMode.HALF_UP);
        audit.record("INVOICE_PAID id=" + invoice.id() + " amount=" + amount.toPlainString());
        response.send(invoice);
    }
}
