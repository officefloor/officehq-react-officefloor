package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/clients/{clientId}/payments — record a single lump sum a client paid and split it across
 * several of that client's invoices. The lump is stored once ({@link ClientPayment}); each allocated
 * share becomes an ordinary {@link Payment} against its invoice, so every invoice's balance and
 * derived status ({@link InvoiceStatus}) comes out right — just as a single payment would settle one.
 * The allocated shares must add up to the lump amount, each share must be positive, and every targeted
 * invoice must belong to the client. A bad amount/date, empty or mismatched allocations, or an invoice
 * that is not the client's, is rejected with 400 and nothing is written.
 */
public class AllocatePaymentLogic {

    public void service(@HttpPathParameter("clientId") String clientId,
            @RequestBody NewClientPayment newPayment, ClientRepository clients,
            ProjectRepository projects, InvoiceRepository invoices, PaymentRepository payments,
            ClientPaymentRepository clientPayments, Audit audit,
            ObjectResponse<ClientPaymentView> response) {
        Long id = Long.valueOf(clientId);
        clients.findById(id).orElseThrow(
                () -> new HttpException(HttpStatus.BAD_REQUEST, "An existing client is required"));

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

        List<NewClientPayment.Allocation> allocations = newPayment.getAllocations();
        if (allocations == null || allocations.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST,
                    "At least one invoice allocation is required");
        }

        // Which invoices are the client's — an allocation may only target one of them.
        Set<Long> clientInvoiceIds = new HashSet<>();
        for (Project project : projects.findByClientIdOrderByIdAsc(id)) {
            for (Invoice inv : invoices.findByProjectIdOrderByIdAsc(project.getId())) {
                clientInvoiceIds.add(inv.getId());
            }
        }

        BigDecimal allocated = BigDecimal.ZERO;
        for (NewClientPayment.Allocation allocation : allocations) {
            Long invoiceId = allocation.getInvoiceId();
            BigDecimal share = allocation.getAmount();
            if (invoiceId == null || !clientInvoiceIds.contains(invoiceId)) {
                throw new HttpException(HttpStatus.BAD_REQUEST,
                        "Each allocation must target one of the client's invoices");
            }
            if (share == null || share.compareTo(BigDecimal.ZERO) <= 0) {
                throw new HttpException(HttpStatus.BAD_REQUEST,
                        "Each allocation amount must be greater than zero");
            }
            allocated = allocated.add(share);
        }
        if (allocated.compareTo(amount) != 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST,
                    "The allocations must add up to the payment amount");
        }

        // Record the lump once, then settle each invoice with its share, linked back to the lump.
        ClientPayment lump = clientPayments.save(new ClientPayment(id, amount, paidDate));
        for (NewClientPayment.Allocation allocation : allocations) {
            payments.save(new Payment(allocation.getInvoiceId(), allocation.getAmount(), paidDate,
                    lump.getId()));
        }
        audit.record("PAYMENT_ALLOCATED id=" + lump.getId() + " amount="
                + amount.setScale(2, RoundingMode.HALF_UP).toPlainString() + " invoices="
                + allocations.size());
        response.send(new ClientPaymentView(lump.getId(), id, amount, paidDate.toString(),
                allocations.size()));
    }
}
