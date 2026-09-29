package net.officefloor.hq.app.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import net.officefloor.hq.app.Audit;
import net.officefloor.hq.app.invoice.Payment;
import net.officefloor.hq.app.invoice.PaymentRepository;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/clients/{id}/payments — record one lump-sum client payment split across several of the
 * client's invoices. Each allocation becomes its own payment row against its invoice, so every
 * invoice's balance comes out right (its due is that invoice's amount less the share applied to it).
 * All the shares of the one lump sum carry a shared batch reference so they can be told apart from
 * unrelated payments. Recording a payment against a single invoice still goes through
 * /api/invoices/{id}/payments as before.
 */
public class SplitPayment {

    public void service(@HttpPathParameter("id") String id, @RequestBody NewSplitPayment body,
            PaymentRepository repository, JdbcTemplate jdbc, Audit audit,
            ObjectResponse<SplitPaymentView> response) {
        String date = body.getDate() == null ? "" : body.getDate().trim();
        LocalDate paidDate;
        try {
            paidDate = LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A payment requires a valid date");
        }
        List<NewSplitPayment.Allocation> allocations =
                body.getAllocations() == null ? List.of() : body.getAllocations();

        // One batch reference ties this lump sum's shares together (one more than the highest so far).
        Long batch = jdbc.queryForObject(
                "SELECT COALESCE(MAX(batch_ref), 0) + 1 FROM payments", Long.class);

        int count = 0;
        BigDecimal total = BigDecimal.ZERO;
        for (NewSplitPayment.Allocation a : allocations) {
            // A blank or non-positive share is simply not part of this split.
            if (a.getInvoiceId() == null || a.getAmount() == null || a.getAmount().signum() <= 0) {
                continue;
            }
            Payment payment = new Payment();
            payment.setInvoiceId(a.getInvoiceId());
            payment.setAmount(a.getAmount());
            payment.setPaidDate(paidDate);
            payment.setBatchRef(batch);
            Payment saved = repository.save(payment);
            // One record per share, in the same shape as a single recorded payment, so the audited
            // amount is the share applied to that invoice.
            audit.record("PAYMENT_RECORDED id=" + saved.getId() + " amount="
                    + saved.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString());
            count++;
            total = total.add(a.getAmount());
        }
        if (count == 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST,
                    "A payment must be allocated to at least one invoice");
        }
        response.send(new SplitPaymentView(batch, count, total));
    }
}
