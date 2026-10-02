package net.officefloor.hq.app.invoices;

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** POST /api/projects/{projectId}/invoices — add an invoice for the project and return the row. */
public class CreateInvoiceLogic {

    public void service(@HttpPathParameter("projectId") String projectId, NewInvoice body,
            InvoiceService invoices, ObjectResponse<InvoiceView> response) {
        response.send(invoices.create(Long.valueOf(projectId), body.getAmount()));
    }
}
