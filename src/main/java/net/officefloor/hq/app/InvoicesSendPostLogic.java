package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/invoices/send — send a draft invoice. Flips its status from DRAFT to SENT and records
 * one audit line (INVOICE_SENT id=&lt;id&gt; amount=&lt;amount&gt;) so the send can be checked back
 * later. Only a draft invoice can be sent.
 */
public class InvoicesSendPostLogic {

    public void service(@RequestBody SendInvoice body, InvoiceRepository repository, Audit audit,
            ObjectResponse<Invoice> response) {
        if (body.getId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "An invoice id is required.");
        }
        Invoice invoice = repository.findById(body.getId());
        if (invoice == null) {
            throw new HttpException(HttpStatus.NOT_FOUND, "No such invoice.");
        }
        if (!"DRAFT".equals(invoice.status())) {
            throw new HttpException(HttpStatus.CONFLICT, "Only a draft invoice can be sent.");
        }
        Invoice sent = repository.markSent(invoice.id());
        BigDecimal amount = sent.amount().setScale(2, RoundingMode.HALF_UP);
        audit.record("INVOICE_SENT id=" + sent.id() + " amount=" + amount.toPlainString());
        response.send(sent);
    }
}
