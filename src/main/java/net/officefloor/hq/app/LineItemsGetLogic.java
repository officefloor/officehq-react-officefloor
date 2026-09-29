package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/lineitems?invoiceId=&lt;id&gt; — list an invoice's line items. */
public class LineItemsGetLogic {

    public void service(@HttpQueryParameter("invoiceId") String invoiceId,
            LineItemRepository repository, ObjectResponse<List<LineItem>> response) {
        long id = invoiceId == null || invoiceId.isBlank() ? 0 : Long.parseLong(invoiceId.trim());
        response.send(repository.findByInvoice(id));
    }
}
