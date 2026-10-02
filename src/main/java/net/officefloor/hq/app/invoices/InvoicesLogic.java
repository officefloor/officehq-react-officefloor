package net.officefloor.hq.app.invoices;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/projects/{projectId}/invoices — list the invoices raised against a project. */
public class InvoicesLogic {

    public void service(@HttpPathParameter("projectId") String projectId, InvoiceService invoices,
            ObjectResponse<List<InvoiceView>> response) {
        response.send(invoices.listForProject(Long.valueOf(projectId)));
    }
}
