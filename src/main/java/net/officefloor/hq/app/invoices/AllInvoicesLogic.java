package net.officefloor.hq.app.invoices;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/invoices — list every invoice across all projects, with its project name and stage.
 * Optionally narrowed to a single lifecycle stage via the {@code status} query parameter.
 */
public class AllInvoicesLogic {

    public void service(@HttpQueryParameter("status") String status, InvoiceService invoices,
            ObjectResponse<List<AllInvoiceView>> response) {
        response.send(invoices.listAll(status));
    }
}
