package net.officefloor.hq.app;

import java.util.List;
import java.util.stream.Collectors;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/invoices/{invoiceId}/lineitems — the line items an invoice charges for, oldest first.
 * The UI works out each line's total and the invoice amount (the sum of them) from these.
 */
public class ListLineItemsLogic {

    public void service(@HttpPathParameter("invoiceId") String invoiceId,
            LineItemRepository lineItems, ObjectResponse<List<LineItemView>> response) {
        Long id = Long.valueOf(invoiceId);
        List<LineItemView> views = lineItems.findByInvoiceIdOrderByIdAsc(id).stream()
                .map(li -> new LineItemView(li.getId(), li.getInvoiceId(), li.getDescription(),
                        li.getQuantity(), li.getUnitPrice()))
                .collect(Collectors.toList());
        response.send(views);
    }
}
