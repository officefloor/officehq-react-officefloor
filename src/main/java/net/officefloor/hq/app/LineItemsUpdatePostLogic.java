package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/lineitems/update — change an existing charge line; the total recomputes. */
public class LineItemsUpdatePostLogic {

    public void service(@RequestBody ChangeLineItem body, LineItemRepository repository,
            ObjectResponse<LineItem> response) {
        if (body.getId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A line item id is required.");
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
        LineItem updated = repository.update(body.getId(), description, body.getQty(), unit,
                unitPrice.setScale(2, RoundingMode.HALF_UP));
        if (updated == null) {
            throw new HttpException(HttpStatus.NOT_FOUND, "No such line item.");
        }
        response.send(updated);
    }
}
