package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/payments — record a payment against an invoice from the request body. Recording a
 * payment is what drives the invoice's status (PARTIAL/PAID); it replaces flipping the invoice to
 * paid by hand, and records one audit line (PAYMENT_RECORDED id=&lt;id&gt; amount=&lt;amount&gt;)
 * so the payment can be checked back later.
 */
public class PaymentsPostLogic {

    public void service(@RequestBody NewPayment body, PaymentRepository repository, Audit audit,
            ObjectResponse<Payment> response) {
        if (body.getInvoiceId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "An invoice is required.");
        }
        BigDecimal amount = body.getAmount();
        if (amount == null || amount.signum() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A positive amount is required.");
        }
        String date = body.getDate() == null ? "" : body.getDate().trim();
        if (date.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A date is required.");
        }
        Payment payment = repository.create(body.getInvoiceId(),
                amount.setScale(2, RoundingMode.HALF_UP), date);
        audit.record("PAYMENT_RECORDED id=" + payment.id() + " amount="
                + payment.amount().toPlainString());
        response.send(payment);
    }
}
