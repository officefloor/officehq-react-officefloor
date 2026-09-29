package net.officefloor.hq.app.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.officefloor.hq.app.Audit;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * POST /api/invoices/{id}/cancel — cancel (void) an invoice that was sent by mistake: flip its
 * status from SENT to VOID and record the fact to the audit file. A voided invoice no longer counts
 * toward what is owed (the dashboard's outstanding total counts only SENT invoices). Only a sent
 * invoice can be cancelled. The audited amount is the invoice total (the sum of its line items),
 * matching how the amount is derived everywhere else.
 */
public class CancelInvoice {

    public void service(@HttpPathParameter("id") String id, InvoiceRepository repository,
            JdbcTemplate jdbc, Audit audit, ObjectResponse<InvoiceView> response) {
        Long invoiceId = Long.valueOf(id);
        Invoice invoice = repository.findById(invoiceId)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "No such invoice"));
        if (!"SENT".equals(invoice.getStatus())) {
            throw new HttpException(HttpStatus.CONFLICT, "Only a sent invoice can be cancelled");
        }
        invoice.setStatus("VOID");
        Invoice saved = repository.save(invoice);
        BigDecimal amount = jdbc.queryForObject(
                "SELECT COALESCE(SUM(qty * unit_price), 0) FROM line_items WHERE invoice_id = ?",
                BigDecimal.class, saved.getId()).setScale(2, RoundingMode.HALF_UP);
        audit.record("INVOICE_VOIDED id=" + saved.getId() + " amount=" + amount.toPlainString());
        response.send(new InvoiceView(saved.getId(), saved.getProjectId(), amount, amount,
                saved.getStatus(),
                saved.getIssuedDate() == null ? null : saved.getIssuedDate().toString(),
                saved.getDueDate() == null ? null : saved.getDueDate().toString()));
    }
}
