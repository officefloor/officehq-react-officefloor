package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/payments — record a payment against an invoice from the request body. */
public class PaymentsPostLogic {

    public void service(@RequestBody NewPayment body, PaymentRepository repository,
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
        response.send(repository.create(body.getInvoiceId(),
                amount.setScale(2, RoundingMode.HALF_UP), date));
    }
}
