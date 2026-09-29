package net.officefloor.hq.app.project;

import java.math.BigDecimal;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/projects/{id}/budget — set the agreed budget on a project. The amount must be zero or
 * more. Returns the updated project.
 */
public class SetProjectBudget {

    public void service(@HttpPathParameter("id") String id, @RequestBody NewBudget body,
            ProjectRepository repository, ObjectResponse<Project> response) {
        BigDecimal budget = body.getBudget();
        if (budget == null || budget.signum() < 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A budget must be zero or more");
        }
        Project project = repository.findById(Long.valueOf(id))
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "No such project"));
        project.setBudget(budget);
        response.send(repository.save(project));
    }
}
