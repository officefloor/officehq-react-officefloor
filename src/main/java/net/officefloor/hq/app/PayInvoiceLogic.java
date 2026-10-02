package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/invoices/{invoiceId}/pay — mark an invoice PAID and record the transition. Flips the
 * invoice's status to PAID, then appends one audit record (INVOICE_PAID id=&lt;id&gt; amount=&lt;amount&gt;)
 * so the owner can check back later. An unknown invoice is rejected with 400 and nothing is written.
 */
public class PayInvoiceLogic {

    public void service(@HttpPathParameter("invoiceId") String invoiceId,
            InvoiceRepository invoices, Audit audit, ObjectResponse<InvoiceView> response) {
        Long id = Long.valueOf(invoiceId);
        Invoice invoice = invoices.findById(id).orElseThrow(
                () -> new HttpException(HttpStatus.BAD_REQUEST, "An existing invoice is required"));
        invoice.setStatus("PAID");
        Invoice saved = invoices.save(invoice);
        BigDecimal amount = saved.getAmount().setScale(2, RoundingMode.HALF_UP);
        audit.record("INVOICE_PAID id=" + saved.getId() + " amount=" + amount.toPlainString());
        response.send(new InvoiceView(saved.getId(), saved.getProjectId(), saved.getAmount(), saved.getStatus(),
                saved.getIssuedDate() == null ? null : saved.getIssuedDate().toString(),
                saved.getDueDate() == null ? null : saved.getDueDate().toString()));
    }
}
