package net.officefloor.hq.app.invoices;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    public List<AllInvoiceView> listAll() {
        return jdbc.query(
                "SELECT i.id, i.amount, i.status, p.name AS project_name "
                        + "FROM invoices i JOIN projects p ON i.project_id = p.id "
                        + "ORDER BY i.id ASC",
                (rs, i) -> new AllInvoiceView(rs.getLong("id"), rs.getString("project_name"),
                        rs.getBigDecimal("amount"), rs.getString("status")));
    }

    @Transactional(readOnly = true)
    public List<InvoiceView> listForProject(Long projectId) {
        return jdbc.query(
                "SELECT id, project_id, amount, status, issued_date, due_date FROM invoices WHERE project_id = ? ORDER BY id ASC",
                (rs, i) -> new InvoiceView(rs.getLong("id"), rs.getLong("project_id"),
                        rs.getBigDecimal("amount"), rs.getString("status"),
                        rs.getDate("issued_date").toLocalDate().toString(),
                        rs.getDate("due_date").toLocalDate().toString()),
                projectId);
    }

    @Transactional(readOnly = true)
    public List<InvoiceView> listForProjectByDueDate(Long projectId) {
        return jdbc.query(
                "SELECT id, project_id, amount, status, issued_date, due_date FROM invoices WHERE project_id = ? ORDER BY due_date ASC, id ASC",
                (rs, i) -> new InvoiceView(rs.getLong("id"), rs.getLong("project_id"),
                        rs.getBigDecimal("amount"), rs.getString("status"),
                        rs.getDate("issued_date").toLocalDate().toString(),
                        rs.getDate("due_date").toLocalDate().toString()),
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
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("An invoice amount must be greater than zero");
        }
        Invoice invoice = new Invoice();
        invoice.setProjectId(projectId);
        invoice.setAmount(amount);
        // A new invoice goes out today and is due 30 days later.
        LocalDate issued = LocalDate.now();
        invoice.setIssuedDate(issued);
        invoice.setDueDate(issued.plusDays(30));
        Invoice saved = repository.save(invoice);
        return new InvoiceView(saved.getId(), saved.getProjectId(), saved.getAmount(),
                saved.getStatus(), saved.getIssuedDate().toString(), saved.getDueDate().toString());
    }

    @Transactional
    public InvoiceView send(Long invoiceId) {
        if (invoiceId == null) {
            throw new IllegalArgumentException("An invoice id is required");
        }
        InvoiceView current = find(invoiceId);
        if (!"DRAFT".equals(current.getStatus())) {
            throw new IllegalStateException("Only a draft invoice can be sent");
        }
        jdbc.update("UPDATE invoices SET status = 'SENT' WHERE id = ?", invoiceId);
        InvoiceView invoice = find(invoiceId);
        audit.record("INVOICE_SENT id=" + invoice.getId() + " amount="
                + invoice.getAmount().setScale(2));
        return invoice;
    }

    @Transactional
    public InvoiceView pay(Long invoiceId) {
        if (invoiceId == null) {
            throw new IllegalArgumentException("An invoice id is required");
        }
        InvoiceView current = find(invoiceId);
        // Payment is only allowed once an invoice has been sent.
        if (!"SENT".equals(current.getStatus())) {
            throw new IllegalStateException("An invoice can only be paid once it has been sent");
        }
        jdbc.update("UPDATE invoices SET status = 'PAID' WHERE id = ?", invoiceId);
        InvoiceView invoice = find(invoiceId);
        audit.record("INVOICE_PAID id=" + invoice.getId() + " amount="
                + invoice.getAmount().setScale(2));
        return invoice;
    }

    private InvoiceView find(Long invoiceId) {
        return jdbc.queryForObject(
                "SELECT id, project_id, amount, status, issued_date, due_date FROM invoices WHERE id = ?",
                (rs, i) -> new InvoiceView(rs.getLong("id"), rs.getLong("project_id"),
                        rs.getBigDecimal("amount"), rs.getString("status"),
                        rs.getDate("issued_date").toLocalDate().toString(),
                        rs.getDate("due_date").toLocalDate().toString()),
                invoiceId);
    }
}
