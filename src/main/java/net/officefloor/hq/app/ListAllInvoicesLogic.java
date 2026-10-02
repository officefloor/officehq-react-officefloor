package net.officefloor.hq.app;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/invoices — every invoice across all projects, oldest first, each carrying the NAME of the
 * project it is for and the stage (status) it is at. Built by joining each invoice to its project.
 */
public class ListAllInvoicesLogic {

    public void service(InvoiceRepository invoices, ProjectRepository projects,
            ObjectResponse<List<AllInvoiceView>> response) {
        Map<Long, String> projectNames = projects.findAll().stream()
                .collect(Collectors.toMap(Project::getId, Project::getName));
        List<AllInvoiceView> views = invoices.findAllByOrderByIdAsc().stream()
                .map(inv -> new AllInvoiceView(inv.getId(), inv.getProjectId(),
                        projectNames.get(inv.getProjectId()), inv.getAmount(), inv.getStatus()))
                .collect(Collectors.toList());
        response.send(views);
    }
}
