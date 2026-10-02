package net.officefloor.hq.app.invoices;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/projects/{projectId}/invoices/by-due — list a project's invoices ordered by due date,
 * earliest due first.
 */
public class InvoicesByDueLogic {

    public void service(@HttpPathParameter("projectId") String projectId, InvoiceService invoices,
            ObjectResponse<List<InvoiceView>> response) {
        response.send(invoices.listForProjectByDueDate(Long.valueOf(projectId)));
    }
}
