package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/dashboard/top-clients — the home screen's five biggest debtors, ranked by how much they
 * owe (largest first). For every active client this sums, across all of that client's projects'
 * invoices, what is still due on each (the invoice's discounted total minus everything paid against
 * it) — the same "money owed" figure {@link ClientOutstandingLogic} totals — then keeps the top five.
 * Ties break by client id ascending so the ordering is stable across loads.
 */
public class DashboardTopClientsLogic {

    private static final int TOP_N = 5;

    public void service(ClientRepository clients, ProjectRepository projects,
            InvoiceRepository invoices, PaymentRepository payments,
            ObjectResponse<List<TopClientView>> response) {
        List<TopClientView> rows = new ArrayList<>();
        for (Client client : clients.findByArchivedFalseOrderByIdAsc()) {
            BigDecimal owed = BigDecimal.ZERO;
            for (Project project : projects.findByClientIdOrderByIdAsc(client.getId())) {
                for (Invoice inv : invoices.findByProjectIdOrderByIdAsc(project.getId())) {
                    BigDecimal paid = payments.findByInvoiceIdOrderByIdAsc(inv.getId()).stream()
                            .map(Payment::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    owed = owed.add(inv.getDiscountedAmount().subtract(paid));
                }
            }
            rows.add(new TopClientView(client.getId(), client.getName(), owed));
        }
        rows.sort(Comparator.comparing(TopClientView::getOwed).reversed()
                .thenComparing(TopClientView::getId));
        response.send(rows.size() > TOP_N ? new ArrayList<>(rows.subList(0, TOP_N)) : rows);
    }
}
