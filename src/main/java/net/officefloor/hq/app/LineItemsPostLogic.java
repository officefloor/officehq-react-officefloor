package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/lineitems — add a line item to an invoice from the request body. */
public class LineItemsPostLogic {

    public void service(@RequestBody NewLineItem body, LineItemRepository repository,
            ObjectResponse<LineItem> response) {
        if (body.getInvoiceId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "An invoice is required.");
        }
        String description = body.getDescription() == null ? "" : body.getDescription().trim();
        if (description.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A description is required.");
        }
        if (body.getQty() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A positive quantity is required.");
        }
        BigDecimal unitPrice = body.getUnitPrice();
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A unit price is required.");
        }
        String unit = body.getUnit() == null ? "" : body.getUnit().trim();
        response.send(repository.create(body.getInvoiceId(), description, body.getQty(), unit,
                unitPrice.setScale(2, RoundingMode.HALF_UP)));
    }
}
