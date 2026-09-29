package net.officefloor.hq.app.invoice;

import java.math.BigDecimal;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/invoices/{id}/line-items — add a thing being charged for (description, quantity, unit
 * price) to an invoice. The invoice's amount is the running sum of its line items.
 */
public class CreateLineItem {

    public void service(@HttpPathParameter("id") String id, @RequestBody NewLineItem body,
            LineItemRepository repository, ObjectResponse<LineItem> response) {
        String description = body.getDescription() == null ? "" : body.getDescription().trim();
        if (description.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A line item requires a description");
        }
        Integer qty = body.getQty();
        if (qty == null || qty <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A line item quantity must be more than zero");
        }
        BigDecimal unitPrice = body.getUnitPrice();
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A line item price cannot be negative");
        }
        String unit = body.getUnit() == null ? "" : body.getUnit().trim();
        LineItem item = new LineItem();
        item.setInvoiceId(Long.valueOf(id));
        item.setDescription(description);
        item.setQty(qty);
        item.setUnit(unit);
        item.setUnitPrice(unitPrice);
        response.send(repository.save(item));
    }
}
