package net.officefloor.hq.app;

import java.math.BigDecimal;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/dashboard — the home summary: the number of clients and projects, and the outstanding
 * total (the money still owed). An invoice counts towards what is owed only once it has actually
 * been SENT to the client: a DRAFT has not been issued yet, and a PAID invoice is settled, so both
 * are excluded. The total is the sum of every SENT invoice's amount across all projects.
 */
public class DashboardLogic {

    public void service(ClientRepository clients, ProjectRepository projects,
            InvoiceRepository invoices, ObjectResponse<DashboardView> response) {
        long clientsCount = clients.count();
        long projectsCount = projects.count();
        BigDecimal outstandingTotal = invoices.findByStatusOrderByIdAsc("SENT").stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.send(new DashboardView(clientsCount, projectsCount, outstandingTotal));
    }
}
