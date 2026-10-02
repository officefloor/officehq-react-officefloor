package net.officefloor.hq.app.invoices;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import net.officefloor.hq.app.Audit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
    private final LineItemRepository lineItemRepository;
    private final PaymentRepository paymentRepository;
    private final JdbcTemplate jdbc;
    private final Audit audit;

    public InvoiceService(InvoiceRepository repository, LineItemRepository lineItemRepository,
            PaymentRepository paymentRepository, JdbcTemplate jdbc, Audit audit) {
        this.repository = repository;
        this.lineItemRepository = lineItemRepository;
        this.paymentRepository = paymentRepository;
        this.jdbc = jdbc;
        this.audit = audit;
    }

    /** Every invoice across all projects; narrowed to a single lifecycle stage when {@code status} is given. */
    @Transactional(readOnly = true)
    public List<AllInvoiceView> listAll(String status) {
        RowMapper<AllInvoiceView> mapper = (rs, i) -> new AllInvoiceView(rs.getLong("id"),
                rs.getString("project_name"), rs.getBigDecimal("amount"), rs.getString("status"));
        String base = "SELECT i.id, i.amount, i.status, p.name AS project_name "
                + "FROM invoices i JOIN projects p ON i.project_id = p.id ";
        if (status == null || status.isBlank()) {
            return jdbc.query(base + "ORDER BY i.id ASC", mapper);
        }
        return jdbc.query(base + "WHERE i.status = ? ORDER BY i.id ASC", mapper, status.trim());
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

    /** The line items of an invoice, in the order they were added. */
    @Transactional(readOnly = true)
    public List<LineItemView> listLineItems(Long invoiceId) {
        return jdbc.query(
                "SELECT id, invoice_id, description, qty, unit_price FROM line_items WHERE invoice_id = ? ORDER BY id ASC",
                (rs, i) -> new LineItemView(rs.getLong("id"), rs.getLong("invoice_id"),
                        rs.getString("description"), rs.getInt("qty"),
                        rs.getBigDecimal("unit_price")),
                invoiceId);
    }

    /**
     * Add one line item to an invoice, then re-derive the invoice's amount as the sum of each line's
     * quantity times unit price so the stored total stays in step. Returns the invoice's line items.
     */
    @Transactional
    public List<LineItemView> addLineItem(Long invoiceId, NewLineItem body) {
        if (invoiceId == null) {
            throw new IllegalArgumentException("An invoice id is required");
        }
        if (body == null || body.getDescription() == null || body.getDescription().isBlank()) {
            throw new IllegalArgumentException("A line item requires a description");
        }
        if (body.getQty() == null || body.getQty() <= 0) {
            throw new IllegalArgumentException("A line item requires a quantity greater than zero");
        }
        if (body.getUnitPrice() == null || body.getUnitPrice().signum() < 0) {
            throw new IllegalArgumentException("A line item requires a unit price that is not negative");
        }
        LineItem lineItem = new LineItem();
        lineItem.setInvoiceId(invoiceId);
        lineItem.setDescription(body.getDescription());
        lineItem.setQty(body.getQty());
        lineItem.setUnitPrice(body.getUnitPrice());
        lineItemRepository.save(lineItem);
        jdbc.update(
                "UPDATE invoices SET amount = COALESCE((SELECT SUM(qty * unit_price) FROM line_items WHERE invoice_id = ?), 0) WHERE id = ?",
                invoiceId, invoiceId);
        return listLineItems(invoiceId);
    }

    /**
     * Remove one line item from an invoice, then re-derive the invoice's amount as the sum of each
     * remaining line's quantity times unit price so the stored total stays in step. Returns the
     * invoice's remaining line items.
     */
    @Transactional
    public List<LineItemView> removeLineItem(Long invoiceId, Long lineItemId) {
        if (invoiceId == null) {
            throw new IllegalArgumentException("An invoice id is required");
        }
        if (lineItemId == null) {
            throw new IllegalArgumentException("A line item id is required");
        }
        jdbc.update("DELETE FROM line_items WHERE id = ? AND invoice_id = ?", lineItemId, invoiceId);
        jdbc.update(
                "UPDATE invoices SET amount = COALESCE((SELECT SUM(qty * unit_price) FROM line_items WHERE invoice_id = ?), 0) WHERE id = ?",
                invoiceId, invoiceId);
        return listLineItems(invoiceId);
    }

    /** The payments a client has made on an invoice, in the order they were recorded. */
    @Transactional(readOnly = true)
    public List<PaymentView> listPayments(Long invoiceId) {
        return jdbc.query(
                "SELECT id, invoice_id, amount, paid_date FROM payments WHERE invoice_id = ? ORDER BY id ASC",
                (rs, i) -> new PaymentView(rs.getLong("id"), rs.getLong("invoice_id"),
                        rs.getBigDecimal("amount"), rs.getDate("paid_date").toLocalDate().toString()),
                invoiceId);
    }

    /** Record one payment a client has made against an invoice. Returns the invoice's payments. */
    @Transactional
    public List<PaymentView> addPayment(Long invoiceId, NewPayment body) {
        if (invoiceId == null) {
            throw new IllegalArgumentException("An invoice id is required");
        }
        if (body == null || body.getAmount() == null || body.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("A payment requires an amount greater than zero");
        }
        if (body.getDate() == null || body.getDate().isBlank()) {
            throw new IllegalArgumentException("A payment requires a date");
        }
        Payment payment = new Payment();
        payment.setInvoiceId(invoiceId);
        payment.setAmount(body.getAmount());
        payment.setPaidDate(LocalDate.parse(body.getDate()));
        paymentRepository.save(payment);
        audit.record("PAYMENT_RECORDED invoice=" + invoiceId + " amount="
                + body.getAmount().setScale(2) + " date=" + body.getDate());
        return listPayments(invoiceId);
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
