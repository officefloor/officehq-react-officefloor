package net.officefloor.hq.app.invoices;

import java.util.List;
import net.officefloor.web.ObjectResponse;

/** GET /api/invoices — list every invoice across all projects, with its project name and stage. */
public class AllInvoicesLogic {

    public void service(InvoiceService invoices, ObjectResponse<List<AllInvoiceView>> response) {
        response.send(invoices.listAll());
    }
}
