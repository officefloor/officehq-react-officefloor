package net.officefloor.hq.app.invoice;

import java.math.BigDecimal;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/invoices/{id}/line-items/{lineItemId}/update — change what a charge line records
 * (description, quantity, price each). The invoice's amount is the running sum of its line items, so
 * changing a line re-derives the total. Same validation as adding a line.
 */
public class UpdateLineItem {

    public void service(@HttpPathParameter("id") String id,
            @HttpPathParameter("lineItemId") String lineItemId, @RequestBody NewLineItem body,
            LineItemRepository repository, ObjectResponse<LineItem> response) {
        Long itemId = Long.valueOf(lineItemId);
        LineItem item = repository.findById(itemId)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "No such line item"));
        if (!item.getInvoiceId().equals(Long.valueOf(id))) {
            throw new HttpException(HttpStatus.NOT_FOUND, "No such line item on this invoice");
        }
        String description = body.getDescription() == null ? "" : body.getDescription().trim();
        if (description.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A line item requires a description");
        }
        Integer qty = body.getQty();
        if (qty == null || qty <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST,
                    "A line item quantity must be more than zero");
        }
        BigDecimal unitPrice = body.getUnitPrice();
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A line item price cannot be negative");
        }
        String unit = body.getUnit() == null ? "" : body.getUnit().trim();
        item.setDescription(description);
        item.setQty(qty);
        item.setUnit(unit);
        item.setUnitPrice(unitPrice);
        response.send(repository.save(item));
    }
}
