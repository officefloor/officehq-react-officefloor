package net.officefloor.hq.app;

import java.util.List;
import java.util.stream.Collectors;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/projects/{projectId}/invoices — a project's invoices, oldest first. */
public class ListInvoicesLogic {

    public void service(@HttpPathParameter("projectId") String projectId,
            InvoiceRepository invoices, ObjectResponse<List<InvoiceView>> response) {
        Long id = Long.valueOf(projectId);
        List<InvoiceView> views = invoices.findByProjectIdOrderByIdAsc(id).stream()
                .map(inv -> new InvoiceView(inv.getId(), inv.getProjectId(), inv.getAmount(), inv.getStatus()))
                .collect(Collectors.toList());
        response.send(views);
    }
}
