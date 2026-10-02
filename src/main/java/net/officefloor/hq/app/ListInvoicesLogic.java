package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/projects/{projectId}/invoices — a project's invoices. Ordered oldest first by default;
 * {@code ?sort=due} orders them by their due date, earliest first (id as a stable tie-breaker).
 */
public class ListInvoicesLogic {

    public void service(@HttpPathParameter("projectId") String projectId,
            @HttpQueryParameter("sort") String sort, InvoiceRepository invoices,
            PaymentRepository payments, ProjectRepository projects, ClientRepository clients,
            ObjectResponse<List<InvoiceView>> response) {
        Long id = Long.valueOf(projectId);
        // The money is shown in the owning client's currency (V35): project -> client -> currency.
        String currency = projects.findById(id)
                .flatMap(project -> clients.findById(project.getClientId()))
                .map(Client::getCurrency)
                .orElse("USD");
        List<Invoice> ordered = "due".equals(sort)
                ? invoices.findByProjectIdOrderByDueDateAscIdAsc(id)
                : invoices.findByProjectIdOrderByIdAsc(id);
        List<InvoiceView> views = ordered.stream()
                .map(inv -> {
                    // What is still left to pay: the billed amount minus everything paid so far.
                    BigDecimal paid = payments.findByInvoiceIdOrderByIdAsc(inv.getId()).stream()
                            .map(Payment::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal due = inv.getAmount().subtract(paid);
                    // Status follows the payments: PAID once covered, PARTIAL once some is paid.
                    String status = InvoiceStatus.derive(inv.getStatus(), inv.getAmount(), paid);
                    InvoiceView view = new InvoiceView(inv.getId(), inv.getProjectId(),
                            inv.getAmount(), due, status,
                            inv.getIssuedDate() == null ? null : inv.getIssuedDate().toString(),
                            inv.getDueDate() == null ? null : inv.getDueDate().toString());
                    view.setCurrency(currency);
                    return view;
                })
                .collect(Collectors.toList());
        response.send(views);
    }
}
