package net.officefloor.hq.app;

import java.math.BigDecimal;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/invoices/{invoiceId}/lineitems — add a line item (description, quantity, unit price) to
 * an existing invoice, then re-total the invoice from its line items and echo back the saved line.
 * A missing/blank description, a non-positive quantity or a negative unit price, or an unknown
 * invoice, is rejected with 400 and nothing is written.
 */
public class CreateLineItemLogic {

    public void service(@HttpPathParameter("invoiceId") String invoiceId,
            @RequestBody NewLineItem newLineItem, InvoiceRepository invoices,
            LineItemRepository lineItems, ObjectResponse<LineItemView> response) {
        Long id = Long.valueOf(invoiceId);
        Invoice invoice = invoices.findById(id).orElseThrow(
                () -> new HttpException(HttpStatus.BAD_REQUEST, "An existing invoice is required"));
        String description = newLineItem.getDescription();
        if (description == null || description.isBlank()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A description is required");
        }
        Integer quantity = newLineItem.getQuantity();
        if (quantity == null || quantity <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A quantity greater than zero is required");
        }
        BigDecimal unitPrice = newLineItem.getUnitPrice();
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A unit price of zero or more is required");
        }
        LineItem saved = lineItems.save(new LineItem(id, description.trim(), quantity, unitPrice));

        // Keep the invoice's amount in step with its line items: the total the owner is charging.
        BigDecimal amount = lineItems.findByInvoiceIdOrderByIdAsc(id).stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        invoice.setAmount(amount);
        invoices.save(invoice);

        response.send(new LineItemView(saved.getId(), saved.getInvoiceId(), saved.getDescription(),
                saved.getQuantity(), saved.getUnitPrice()));
    }
}
