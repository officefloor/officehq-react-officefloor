package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.ObjectResponse;

/** GET /api/invoices/all — every invoice across all projects, with its project name and stage. */
public class AllInvoicesGetLogic {

    public void service(InvoiceRepository repository, ObjectResponse<List<InvoiceListing>> response) {
        response.send(repository.findAllWithProject());
    }
}
