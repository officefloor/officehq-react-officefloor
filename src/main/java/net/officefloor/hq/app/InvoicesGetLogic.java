package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/invoices?projectId=&lt;id&gt; — list a project's invoices. */
public class InvoicesGetLogic {

    public void service(@HttpQueryParameter("projectId") String projectId,
            @HttpQueryParameter("sort") String sort, InvoiceRepository repository,
            ObjectResponse<List<Invoice>> response) {
        long id = projectId == null || projectId.isBlank() ? 0 : Long.parseLong(projectId.trim());
        if (sort != null && "due".equalsIgnoreCase(sort.trim())) {
            response.send(repository.findByProjectOrderByDueDate(id));
        } else {
            response.send(repository.findByProject(id));
        }
    }
}
