package net.officefloor.hq.app;

import java.math.BigDecimal;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/dashboard — the home summary: the number of clients and projects, and the outstanding
 * total (the sum of every UNPAID invoice's amount across all projects — the money still owed).
 */
public class DashboardLogic {

    public void service(ClientRepository clients, ProjectRepository projects,
            InvoiceRepository invoices, ObjectResponse<DashboardView> response) {
        long clientsCount = clients.count();
        long projectsCount = projects.count();
        BigDecimal outstandingTotal = invoices.findByStatusOrderByIdAsc("UNPAID").stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.send(new DashboardView(clientsCount, projectsCount, outstandingTotal));
    }
}
