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
            PaymentRepository payments, ObjectResponse<List<InvoiceView>> response) {
        Long id = Long.valueOf(projectId);
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
                    return new InvoiceView(inv.getId(), inv.getProjectId(), inv.getAmount(), due,
                            inv.getStatus(),
                            inv.getIssuedDate() == null ? null : inv.getIssuedDate().toString(),
                            inv.getDueDate() == null ? null : inv.getDueDate().toString());
                })
                .collect(Collectors.toList());
        response.send(views);
    }
}
