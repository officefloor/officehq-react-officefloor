package net.officefloor.hq.app.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.officefloor.hq.app.Audit;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/invoices/{id}/pay — mark an invoice paid: flip its status to PAID and record the fact
 * to the audit file so it can be checked back later.
 */
public class PayInvoice {

    public void service(@HttpPathParameter("id") String id, InvoiceRepository repository,
            Audit audit, ObjectResponse<InvoiceView> response) {
        Long invoiceId = Long.valueOf(id);
        Invoice invoice = repository.findById(invoiceId)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "No such invoice"));
        invoice.setStatus("PAID");
        Invoice saved = repository.save(invoice);
        BigDecimal amount = saved.getAmount().setScale(2, RoundingMode.HALF_UP);
        audit.record("INVOICE_PAID id=" + saved.getId() + " amount=" + amount.toPlainString());
        response.send(new InvoiceView(saved.getId(), saved.getProjectId(), saved.getAmount(),
                saved.getStatus(),
                saved.getIssuedDate() == null ? null : saved.getIssuedDate().toString(),
                saved.getDueDate() == null ? null : saved.getDueDate().toString()));
    }
}
