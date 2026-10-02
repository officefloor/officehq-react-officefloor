package net.officefloor.hq.app;

import java.util.List;
import java.util.stream.Collectors;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/invoices/{invoiceId}/payments — the payments a client has made against an invoice, oldest
 * first. Each carries the amount paid and the date it was paid.
 */
public class ListPaymentsLogic {

    public void service(@HttpPathParameter("invoiceId") String invoiceId,
            PaymentRepository payments, ObjectResponse<List<PaymentView>> response) {
        Long id = Long.valueOf(invoiceId);
        List<PaymentView> views = payments.findByInvoiceIdOrderByIdAsc(id).stream()
                .map(p -> new PaymentView(p.getId(), p.getInvoiceId(), p.getAmount(),
                        p.getPaidDate().toString()))
                .collect(Collectors.toList());
        response.send(views);
    }
}
