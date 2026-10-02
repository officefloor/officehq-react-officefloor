package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/invoices/{invoiceId}/payments — record a payment (amount and date) a client has made
 * against an existing invoice, and echo back the saved payment. A non-positive amount, a
 * missing/unparseable date, or an unknown invoice, is rejected with 400 and nothing is written.
 */
public class RecordPaymentLogic {

    public void service(@HttpPathParameter("invoiceId") String invoiceId,
            @RequestBody NewPayment newPayment, InvoiceRepository invoices,
            PaymentRepository payments, ObjectResponse<PaymentView> response) {
        Long id = Long.valueOf(invoiceId);
        invoices.findById(id).orElseThrow(
                () -> new HttpException(HttpStatus.BAD_REQUEST, "An existing invoice is required"));
        BigDecimal amount = newPayment.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "An amount greater than zero is required");
        }
        String date = newPayment.getDate();
        if (date == null || date.isBlank()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A payment date is required");
        }
        LocalDate paidDate;
        try {
            paidDate = LocalDate.parse(date.trim());
        } catch (DateTimeParseException e) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A valid payment date is required");
        }
        Payment saved = payments.save(new Payment(id, amount, paidDate));
        response.send(new PaymentView(saved.getId(), saved.getInvoiceId(), saved.getAmount(),
                saved.getPaidDate().toString()));
    }
}
