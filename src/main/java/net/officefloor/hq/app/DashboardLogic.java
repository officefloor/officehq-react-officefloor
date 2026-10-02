package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/dashboard — the home summary: the number of clients and projects, the outstanding total
 * (the money still owed) and how many SENT invoices are overdue. An invoice counts towards what is
 * owed only once it has actually been SENT to the client: a DRAFT has not been issued yet, and a PAID
 * invoice is settled, so both are excluded. The total is the sum of every SENT invoice's final total
 * (its discount already taken off), so the money owed reflects the discount. An
 * invoice is overdue when it is SENT and its due date has passed relative to the dashboard's
 * reference ("as of") date; that date comes from the dashboard_config row, falling back to today.
 */
public class DashboardLogic {

    public void service(ClientRepository clients, ProjectRepository projects,
            InvoiceRepository invoices, DashboardConfigRepository config,
            ObjectResponse<DashboardView> response) {
        long clientsCount = clients.count();
        long projectsCount = projects.count();
        BigDecimal outstandingTotal = invoices.findByStatusOrderByIdAsc("SENT").stream()
                .map(Invoice::getDiscountedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        LocalDate asOf = config.findById(1L)
                .map(DashboardConfig::getAsOf)
                .orElse(LocalDate.now());
        long overdueCount = invoices.countByStatusAndDueDateBefore("SENT", asOf);
        response.send(new DashboardView(clientsCount, projectsCount, outstandingTotal, overdueCount));
    }
}
