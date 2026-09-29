package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/payments/split — record one lump payment split across several invoices. Each allocation
 * with a positive share becomes its own {@link Payment} row, so every invoice's derived balance and
 * status settle from the payments on file just as a single payment does. One audit line per created
 * payment (PAYMENT_RECORDED id=&lt;id&gt; amount=&lt;amount&gt;), matching a single payment, so a
 * split reads back the same way.
 */
public class PaymentsSplitPostLogic {

    public void service(@RequestBody NewSplitPayment body, PaymentRepository repository, Audit audit,
            ObjectResponse<List<Payment>> response) {
        String date = body.getDate() == null ? "" : body.getDate().trim();
        if (date.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A date is required.");
        }
        List<PaymentAllocation> allocations =
                body.getAllocations() == null ? List.of() : body.getAllocations();
        List<Payment> created = new ArrayList<>();
        for (PaymentAllocation allocation : allocations) {
            BigDecimal amount = allocation.getAmount();
            // An allocation left blank or zero simply does not apply to that invoice; skip it so a
            // client can spread a lump sum over only some of their open invoices.
            if (amount == null || amount.signum() <= 0) {
                continue;
            }
            if (allocation.getInvoiceId() <= 0) {
                throw new HttpException(HttpStatus.BAD_REQUEST, "An invoice is required.");
            }
            Payment payment = repository.create(allocation.getInvoiceId(),
                    amount.setScale(2, RoundingMode.HALF_UP), date);
            audit.record("PAYMENT_RECORDED id=" + payment.id() + " amount="
                    + payment.amount().toPlainString());
            created.add(payment);
        }
        if (created.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A positive amount is required.");
        }
        response.send(created);
    }
}
