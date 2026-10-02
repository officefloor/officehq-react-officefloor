package net.officefloor.hq.app;

import java.math.BigDecimal;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * DELETE /api/invoices/{invoiceId}/lineitems/{lineItemId} — remove a charge line from an invoice,
 * then re-total the invoice from its remaining line items and echo back the updated invoice. An
 * unknown invoice, an unknown line item, or a line item that does not belong to the invoice, is
 * rejected with 400 and nothing is written.
 */
public class DeleteLineItemLogic {

    public void service(@HttpPathParameter("invoiceId") String invoiceId,
            @HttpPathParameter("lineItemId") String lineItemId, InvoiceRepository invoices,
            LineItemRepository lineItems, ObjectResponse<InvoiceView> response) {
        Long id = Long.valueOf(invoiceId);
        Invoice invoice = invoices.findById(id).orElseThrow(
                () -> new HttpException(HttpStatus.BAD_REQUEST, "An existing invoice is required"));
        Long lineId = Long.valueOf(lineItemId);
        LineItem lineItem = lineItems.findById(lineId).orElseThrow(
                () -> new HttpException(HttpStatus.BAD_REQUEST, "An existing line item is required"));
        if (!id.equals(lineItem.getInvoiceId())) {
            throw new HttpException(HttpStatus.BAD_REQUEST,
                    "The line item does not belong to this invoice");
        }
        lineItems.deleteById(lineId);

        // Keep the invoice's amount in step with its line items: the total the owner is charging.
        BigDecimal amount = lineItems.findByInvoiceIdOrderByIdAsc(id).stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        invoice.setAmount(amount);
        Invoice saved = invoices.save(invoice);

        response.send(new InvoiceView(saved.getId(), saved.getProjectId(), saved.getAmount(),
                saved.getStatus(),
                saved.getIssuedDate() == null ? null : saved.getIssuedDate().toString(),
                saved.getDueDate() == null ? null : saved.getDueDate().toString()));
    }
}
