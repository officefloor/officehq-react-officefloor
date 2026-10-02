package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/clients/{clientId}/statement — one client's statement: all of that client's invoices,
 * gathered from every one of their projects, each with how much is still left to pay (the billed
 * amount minus everything paid against it), and the outstanding total — the sum of what is still due
 * across those invoices.
 */
public class ClientStatementLogic {

    public void service(@HttpPathParameter("clientId") String clientId, ProjectRepository projects,
            InvoiceRepository invoices, PaymentRepository payments,
            ObjectResponse<ClientStatementView> response) {
        Long id = Long.valueOf(clientId);
        List<InvoiceView> rows = new ArrayList<>();
        BigDecimal outstandingTotal = BigDecimal.ZERO;
        for (Project project : projects.findByClientIdOrderByIdAsc(id)) {
            for (Invoice inv : invoices.findByProjectIdOrderByIdAsc(project.getId())) {
                // What is still left to pay: the billed amount minus everything paid so far.
                BigDecimal paid = payments.findByInvoiceIdOrderByIdAsc(inv.getId()).stream()
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal due = inv.getAmount().subtract(paid);
                String status = InvoiceStatus.derive(inv.getStatus(), inv.getAmount(), paid);
                rows.add(new InvoiceView(inv.getId(), inv.getProjectId(), inv.getAmount(), due,
                        status,
                        inv.getIssuedDate() == null ? null : inv.getIssuedDate().toString(),
                        inv.getDueDate() == null ? null : inv.getDueDate().toString()));
                outstandingTotal = outstandingTotal.add(due);
            }
        }
        response.send(new ClientStatementView(rows, outstandingTotal));
    }
}
