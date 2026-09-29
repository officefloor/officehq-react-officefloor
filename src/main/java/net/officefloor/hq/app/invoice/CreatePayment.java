package net.officefloor.hq.app.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import net.officefloor.hq.app.Audit;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/invoices/{id}/payments — record what a client has paid against an invoice: an amount
 * and the date it was paid. One row per payment; an invoice can have many.
 */
public class CreatePayment {

    public void service(@HttpPathParameter("id") String id, @RequestBody NewPayment body,
            PaymentRepository repository, Audit audit, ObjectResponse<Payment> response) {
        BigDecimal amount = body.getAmount();
        if (amount == null || amount.signum() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A payment amount must be more than zero");
        }
        String date = body.getDate() == null ? "" : body.getDate().trim();
        LocalDate paidDate;
        try {
            paidDate = LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A payment requires a valid date");
        }
        Payment payment = new Payment();
        payment.setInvoiceId(Long.valueOf(id));
        payment.setAmount(amount);
        payment.setPaidDate(paidDate);
        Payment saved = repository.save(payment);
        // Recording the payment is what drives the invoice's status now, so the fact is audited here
        // (the amount to two decimal places) instead of on a manual mark-paid.
        audit.record("PAYMENT_RECORDED id=" + saved.getId() + " amount="
                + saved.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString());
        response.send(saved);
    }
}
