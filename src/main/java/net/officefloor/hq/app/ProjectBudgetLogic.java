package net.officefloor.hq.app;

import java.math.BigDecimal;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/projects/{projectId}/budget — a project's budget position: the budget set on it, how much
 * has been invoiced against it (the sum of its invoices) and what is left (budget minus invoiced).
 */
public class ProjectBudgetLogic {

    public void service(@HttpPathParameter("projectId") String projectId,
            ProjectRepository projects, InvoiceRepository invoices,
            ObjectResponse<ProjectBudgetView> response) {
        Long id = Long.valueOf(projectId);
        Project project = projects.findById(id).orElseThrow(
                () -> new HttpException(HttpStatus.NOT_FOUND, "Unknown project"));
        BigDecimal invoiced = invoices.findByProjectIdOrderByIdAsc(id).stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.send(new ProjectBudgetView(project.getBudget(), invoiced));
    }
}
