package net.officefloor.hq.app;

import java.math.BigDecimal;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/invoices — add an invoice to a project from the request body. */
public class InvoicesPostLogic {

    public void service(@RequestBody NewInvoice body, InvoiceRepository repository,
            ObjectResponse<Invoice> response) {
        if (body.getProjectId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A project is required.");
        }
        BigDecimal amount = body.getAmount();
        if (amount == null || amount.signum() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A positive amount is required.");
        }
        response.send(repository.create(body.getProjectId(), amount.setScale(2,
                java.math.RoundingMode.HALF_UP)));
    }
}
