package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/invoices/{invoiceId}/void — cancel an invoice that was sent by mistake. Flips a SENT
 * invoice's status to VOID so it no longer counts towards what the owner is owed, then appends one
 * audit record (INVOICE_VOIDED id=&lt;id&gt; amount=&lt;amount&gt;) so the cancellation is noted. An
 * unknown invoice, or one that has not been SENT, is rejected with 400 and nothing is written.
 */
public class VoidInvoiceLogic {

    public void service(@HttpPathParameter("invoiceId") String invoiceId,
            InvoiceRepository invoices, Audit audit, ObjectResponse<InvoiceView> response) {
        Long id = Long.valueOf(invoiceId);
        Invoice invoice = invoices.findById(id).orElseThrow(
                () -> new HttpException(HttpStatus.BAD_REQUEST, "An existing invoice is required"));
        if (!"SENT".equals(invoice.getStatus())) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Only a sent invoice can be cancelled");
        }
        invoice.setStatus("VOID");
        Invoice saved = invoices.save(invoice);
        BigDecimal amount = saved.getAmount().setScale(2, RoundingMode.HALF_UP);
        audit.record("INVOICE_VOIDED id=" + saved.getId() + " amount=" + amount.toPlainString());
        response.send(new InvoiceView(saved.getId(), saved.getProjectId(), saved.getAmount(), saved.getStatus(),
                saved.getIssuedDate() == null ? null : saved.getIssuedDate().toString(),
                saved.getDueDate() == null ? null : saved.getDueDate().toString()));
    }
}
