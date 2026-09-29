package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/invoices/cancel — cancel (void) an invoice sent by mistake. Flips its status to VOID so
 * it reads VOID and stops counting toward money owed, and records one audit line
 * (INVOICE_VOIDED id=&lt;id&gt; amount=&lt;amount&gt;) so the cancellation can be checked back later.
 * Only a sent (unpaid) invoice can be cancelled.
 */
public class InvoicesCancelPostLogic {

    public void service(@RequestBody CancelInvoice body, InvoiceRepository repository, Audit audit,
            ObjectResponse<Invoice> response) {
        if (body.getId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "An invoice id is required.");
        }
        Invoice invoice = repository.findById(body.getId());
        if (invoice == null) {
            throw new HttpException(HttpStatus.NOT_FOUND, "No such invoice.");
        }
        if (!"SENT".equals(invoice.status())) {
            throw new HttpException(HttpStatus.CONFLICT, "Only a sent invoice can be cancelled.");
        }
        BigDecimal amount = invoice.amount().setScale(2, RoundingMode.HALF_UP);
        Invoice voided = repository.markVoid(invoice.id());
        audit.record("INVOICE_VOIDED id=" + voided.id() + " amount=" + amount.toPlainString());
        response.send(voided);
    }
}
