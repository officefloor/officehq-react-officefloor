package net.officefloor.hq.app.project;

import java.math.BigDecimal;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * GET /api/projects/{id}/budget — a project's budget picture: the agreed budget, how much has been
 * invoiced against it (the sum of every line item across its invoices), and what is left. When no
 * budget has been set the budget and remaining come back null.
 */
public class GetProjectBudget {

    public void service(@HttpPathParameter("id") String id, JdbcTemplate jdbc,
            ObjectResponse<ProjectBudgetView> response) {
        Long projectId = Long.valueOf(id);
        BigDecimal budget = jdbc.queryForObject(
                "SELECT budget FROM projects WHERE id = ?", BigDecimal.class, projectId);
        BigDecimal invoiced = jdbc.queryForObject(
                "SELECT COALESCE(SUM(li.qty * li.unit_price), 0) FROM line_items li "
                        + "JOIN invoices i ON i.id = li.invoice_id WHERE i.project_id = ?",
                BigDecimal.class, projectId);
        BigDecimal remaining = budget == null ? null : budget.subtract(invoiced);
        response.send(new ProjectBudgetView(budget, invoiced, remaining));
    }
}
