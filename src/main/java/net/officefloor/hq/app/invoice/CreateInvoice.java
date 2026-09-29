package net.officefloor.hq.app.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/invoices — add an invoice with an amount to a project. */
public class CreateInvoice {

    public void service(@RequestBody NewInvoice body, InvoiceRepository repository,
            ObjectResponse<Invoice> response) {
        if (body.getProjectId() == null) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "An invoice requires a project");
        }
        BigDecimal amount = body.getAmount();
        if (amount == null || amount.signum() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "An invoice amount must be more than zero");
        }
        Invoice invoice = new Invoice();
        invoice.setProjectId(body.getProjectId());
        invoice.setAmount(amount);
        invoice.setStatus("DRAFT");
        LocalDate today = LocalDate.now();
        invoice.setIssuedDate(today);
        invoice.setDueDate(today.plusDays(30));
        response.send(repository.save(invoice));
    }
}
