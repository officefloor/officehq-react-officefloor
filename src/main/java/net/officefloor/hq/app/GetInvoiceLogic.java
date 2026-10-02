package net.officefloor.hq.app;

import java.math.BigDecimal;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/invoices/{invoiceId} — a single invoice as the UI shows it, including how much is still
 * owing ({@code due}) and its payment status derived from the payments recorded against it. An
 * unknown invoice is rejected with 400. Used by an opened invoice to show its current status.
 */
public class GetInvoiceLogic {

    public void service(@HttpPathParameter("invoiceId") String invoiceId,
            InvoiceRepository invoices, PaymentRepository payments,
            ObjectResponse<InvoiceView> response) {
        Long id = Long.valueOf(invoiceId);
        Invoice inv = invoices.findById(id).orElseThrow(
                () -> new HttpException(HttpStatus.BAD_REQUEST, "An existing invoice is required"));
        BigDecimal paid = payments.findByInvoiceIdOrderByIdAsc(id).stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal due = inv.getAmount().subtract(paid);
        String status = InvoiceStatus.derive(inv.getStatus(), inv.getAmount(), paid);
        response.send(new InvoiceView(inv.getId(), inv.getProjectId(), inv.getAmount(), due, status,
                inv.getIssuedDate() == null ? null : inv.getIssuedDate().toString(),
                inv.getDueDate() == null ? null : inv.getDueDate().toString(), inv.getDiscountPct(),
                inv.getTaxPct()));
    }
}
