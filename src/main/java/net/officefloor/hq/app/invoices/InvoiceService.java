package net.officefloor.hq.app.invoices;

import java.math.BigDecimal;
import java.util.List;
import net.officefloor.hq.app.Audit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for invoices. Invoices are always viewed in the context of their project, so the
 * list is scoped by project id. The derived total the UI shows is computed by the front-end from the
 * returned rows; this service just surfaces the project's invoices in a stable order. Marking an
 * invoice paid flips its status and records an audit entry so the action can be checked later.
 */
@Service
public class InvoiceService {

    private final InvoiceRepository repository;
    private final JdbcTemplate jdbc;
    private final Audit audit;

    public InvoiceService(InvoiceRepository repository, JdbcTemplate jdbc, Audit audit) {
        this.repository = repository;
        this.jdbc = jdbc;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<InvoiceView> listForProject(Long projectId) {
        return jdbc.query(
                "SELECT id, project_id, amount, status FROM invoices WHERE project_id = ? ORDER BY id ASC",
                (rs, i) -> new InvoiceView(rs.getLong("id"), rs.getLong("project_id"),
                        rs.getBigDecimal("amount"), rs.getString("status")),
                projectId);
    }

    @Transactional
    public InvoiceView create(Long projectId, BigDecimal amount) {
        if (projectId == null) {
            throw new IllegalArgumentException("An invoice requires a project");
        }
        if (amount == null) {
            throw new IllegalArgumentException("An invoice requires an amount");
        }
        Invoice invoice = new Invoice();
        invoice.setProjectId(projectId);
        invoice.setAmount(amount);
        Invoice saved = repository.save(invoice);
        return new InvoiceView(saved.getId(), saved.getProjectId(), saved.getAmount(),
                saved.getStatus());
    }

    @Transactional
    public InvoiceView pay(Long invoiceId) {
        if (invoiceId == null) {
            throw new IllegalArgumentException("An invoice id is required");
        }
        jdbc.update("UPDATE invoices SET status = 'PAID' WHERE id = ?", invoiceId);
        InvoiceView invoice = jdbc.queryForObject(
                "SELECT id, project_id, amount, status FROM invoices WHERE id = ?",
                (rs, i) -> new InvoiceView(rs.getLong("id"), rs.getLong("project_id"),
                        rs.getBigDecimal("amount"), rs.getString("status")),
                invoiceId);
        audit.record("INVOICE_PAID id=" + invoice.getId() + " amount="
                + invoice.getAmount().setScale(2));
        return invoice;
    }
}
