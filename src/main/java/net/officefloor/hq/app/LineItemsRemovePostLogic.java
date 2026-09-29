package net.officefloor.hq.app;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/lineitems/remove — remove a charge line from an invoice; the total recomputes. */
public class LineItemsRemovePostLogic {

    public void service(@RequestBody RemoveLineItem body, LineItemRepository repository,
            ObjectResponse<LineItem> response) {
        if (body.getId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A line item id is required.");
        }
        LineItem existing = repository.findById(body.getId());
        if (existing == null) {
            throw new HttpException(HttpStatus.NOT_FOUND, "No such line item.");
        }
        repository.delete(existing.id());
        response.send(existing);
    }
}
