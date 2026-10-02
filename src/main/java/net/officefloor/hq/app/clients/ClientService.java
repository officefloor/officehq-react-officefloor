package net.officefloor.hq.app.clients;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;
import net.officefloor.hq.app.Audit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Business logic for clients. Each call runs in its own transaction. */
@Service
public class ClientService {

    // A client must carry a proper email address (mirrors the front-end guard).
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final ClientRepository repository;
    private final ContactRepository contacts;
    private final JdbcTemplate jdbc;
    private final Audit audit;

    public ClientService(ClientRepository repository, ContactRepository contacts, JdbcTemplate jdbc,
            Audit audit) {
        this.repository = repository;
        this.contacts = contacts;
        this.jdbc = jdbc;
        this.audit = audit;
    }

    /** List the clients still in play. Archived clients are tucked away and omitted. */
    @Transactional(readOnly = true)
    public List<Client> list() {
        return repository.findByArchivedFalseOrderByIdAsc();
    }

    /**
     * Clients whose name contains {@code query} (case-insensitive); all clients when blank. Archived
     * clients are tucked away, so they drop off the search too.
     */
    @Transactional(readOnly = true)
    public List<Client> search(String query) {
        if (query == null || query.isBlank()) {
            return list();
        }
        return repository.findByArchivedFalseAndNameContainingIgnoreCaseOrderByIdAsc(query.trim());
    }

    /**
     * Archive a client: keep the row (and everything hanging off it) but flag it so it drops off the
     * list and search. Records an audit entry so the action can be checked later, and returns the
     * clients that remain visible.
     */
    @Transactional
    public List<Client> archive(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("A client id is required to archive");
        }
        jdbc.update("UPDATE clients SET archived = TRUE WHERE id = ?", id);
        audit.record("CLIENT_ARCHIVED id=" + id);
        return list();
    }

    /**
     * The projects done for a client, oldest first. By default only ACTIVE, non-archived projects —
     * the work still in play — are returned; pass {@code includeAll} to also surface the finished and
     * archived (hidden) ones (how the client page's "show all" toggle reveals them). Reads the
     * {@code projects} table via SQL so the clients feature stays self-contained and does not import
     * the projects feature's Java types.
     */
    @Transactional(readOnly = true)
    public List<ClientProjectView> projectsFor(Long clientId, boolean includeAll) {
        String filter = includeAll ? "" : " AND status = 'ACTIVE' AND archived = FALSE";
        return jdbc.query(
                "SELECT id, name FROM projects WHERE client_id = ?" + filter + " ORDER BY id ASC",
                (rs, i) -> new ClientProjectView(rs.getLong("id"), rs.getString("name")),
                clientId);
    }

    /**
     * At-a-glance counts for a client's page: how many projects and contacts they have. Counts in
     * SQL so a large client doesn't have to ship its full project/contact lists just to be tallied.
     */
    @Transactional(readOnly = true)
    public ClientCountsView countsFor(Long clientId) {
        Long projects = jdbc.queryForObject(
                "SELECT COUNT(*) FROM projects WHERE client_id = ? AND archived = FALSE",
                Long.class, clientId);
        Long contacts = jdbc.queryForObject(
                "SELECT COUNT(*) FROM contacts WHERE client_id = ?", Long.class, clientId);
        return new ClientCountsView(projects == null ? 0 : projects, contacts == null ? 0 : contacts);
    }

    /**
     * A statement for a client: every invoice raised across all of the client's projects, gathered
     * in one place, each with how much is still due (its amount minus whatever has been paid against
     * it). The outstanding total is the sum of those dues — what the client still owes. Read via SQL
     * so the clients feature stays self-contained and does not import the invoices feature's types.
     */
    @Transactional(readOnly = true)
    public ClientStatementView statementFor(Long clientId) {
        List<ClientStatementInvoiceView> invoices = jdbc.query(
                "SELECT i.id, p.name AS project_name, i.amount, i.status, "
                        + "i.amount - COALESCE((SELECT SUM(pay.amount) FROM payments pay "
                        + "WHERE pay.invoice_id = i.id), 0) AS due_amount "
                        + "FROM invoices i JOIN projects p ON i.project_id = p.id "
                        + "WHERE p.client_id = ? ORDER BY i.id ASC",
                (rs, i) -> new ClientStatementInvoiceView(rs.getLong("id"),
                        rs.getString("project_name"), rs.getBigDecimal("amount"),
                        rs.getString("status"), rs.getBigDecimal("due_amount")),
                clientId);
        BigDecimal outstanding = invoices.stream().map(ClientStatementInvoiceView::getDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ClientStatementView(invoices, outstanding);
    }

    /** The contacts kept for a client, oldest first. */
    @Transactional(readOnly = true)
    public List<ClientContactView> contactsFor(Long clientId) {
        return contacts.findByClientIdOrderByIdAsc(clientId).stream()
                .map(c -> new ClientContactView(c.getId(), c.getClientId(), c.getName(),
                        c.getEmail(), c.getRole()))
                .toList();
    }

    @Transactional
    public ClientContactView createContact(Long clientId, String name, String email, String role) {
        if (clientId == null) {
            throw new IllegalArgumentException("A contact requires a client");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A contact requires a name");
        }
        if (email == null || !EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("A contact requires a valid email address");
        }
        Contact contact = new Contact();
        contact.setClientId(clientId);
        contact.setName(name);
        contact.setEmail(email);
        contact.setRole(role);
        Contact saved = contacts.save(contact);
        return new ClientContactView(saved.getId(), saved.getClientId(), saved.getName(),
                saved.getEmail(), saved.getRole());
    }

    @Transactional
    public Client create(String name, String email) {
        if (email == null || !EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("A client requires a valid email address");
        }
        Client client = new Client();
        client.setName(name);
        client.setEmail(email);
        return repository.save(client);
    }
}
