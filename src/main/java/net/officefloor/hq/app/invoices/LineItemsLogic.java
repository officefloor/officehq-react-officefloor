package net.officefloor.hq.app.invoices;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/invoices/{invoiceId}/line-items — list the things an invoice is charging for. */
public class LineItemsLogic {

    public void service(@HttpPathParameter("invoiceId") String invoiceId, InvoiceService invoices,
            ObjectResponse<List<LineItemView>> response) {
        response.send(invoices.listLineItems(Long.valueOf(invoiceId)));
    }
}
