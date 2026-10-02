package net.officefloor.hq.app;

import java.math.BigDecimal;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/projects/{projectId}/invoices — add an invoice for the given amount to an existing
 * project, echo back the saved row. A missing amount or an unknown project is rejected with 400 and
 * never persisted.
 */
public class CreateInvoiceLogic {

    public void service(@HttpPathParameter("projectId") String projectId,
            @RequestBody NewInvoice newInvoice, ProjectRepository projects,
            InvoiceRepository invoices, ObjectResponse<InvoiceView> response) {
        BigDecimal amount = newInvoice.getAmount();
        if (amount == null) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "An amount is required");
        }
        Long id = Long.valueOf(projectId);
        projects.findById(id).orElseThrow(
                () -> new HttpException(HttpStatus.BAD_REQUEST, "An existing project is required"));
        Invoice saved = invoices.save(new Invoice(id, amount));
        response.send(new InvoiceView(saved.getId(), saved.getProjectId(), saved.getAmount()));
    }
}
