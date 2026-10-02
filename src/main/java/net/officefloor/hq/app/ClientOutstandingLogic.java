package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/clients/outstanding — how much each active client owes, so the clients list can be ordered
 * by it. For every active client this sums, across all of that client's projects' invoices, what is
 * still due on each (the invoice's discounted total minus everything paid against it) — the same
 * "money owed" figure the client statement totals ({@link ClientStatementLogic}).
 */
public class ClientOutstandingLogic {

    public void service(ClientRepository clients, ProjectRepository projects,
            InvoiceRepository invoices, PaymentRepository payments,
            ObjectResponse<List<ClientOutstandingView>> response) {
        List<ClientOutstandingView> rows = new ArrayList<>();
        for (Client client : clients.findByArchivedFalseOrderByIdAsc()) {
            BigDecimal outstanding = BigDecimal.ZERO;
            for (Project project : projects.findByClientIdOrderByIdAsc(client.getId())) {
                for (Invoice inv : invoices.findByProjectIdOrderByIdAsc(project.getId())) {
                    BigDecimal paid = payments.findByInvoiceIdOrderByIdAsc(inv.getId()).stream()
                            .map(Payment::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    outstanding = outstanding.add(inv.getDiscountedAmount().subtract(paid));
                }
            }
            rows.add(new ClientOutstandingView(client.getId(), outstanding));
        }
        response.send(rows);
    }
}
