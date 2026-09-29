package net.officefloor.hq.app.invoice;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/invoices/{id}/line-items/{lineItemId}/remove — drop one charge line from an invoice.
 * The invoice's amount is the running sum of its remaining line items, so removing a line lowers the
 * total. Returns the removed line so the caller can confirm what went.
 */
public class RemoveLineItem {

    public void service(@HttpPathParameter("id") String id,
            @HttpPathParameter("lineItemId") String lineItemId, LineItemRepository repository,
            ObjectResponse<LineItemView> response) {
        Long itemId = Long.valueOf(lineItemId);
        LineItem item = repository.findById(itemId)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "No such line item"));
        if (!item.getInvoiceId().equals(Long.valueOf(id))) {
            throw new HttpException(HttpStatus.NOT_FOUND, "No such line item on this invoice");
        }
        LineItemView removed = new LineItemView(item.getId(), item.getInvoiceId(),
                item.getDescription(), item.getQty(), item.getUnitPrice());
        repository.deleteById(itemId);
        response.send(removed);
    }
}
