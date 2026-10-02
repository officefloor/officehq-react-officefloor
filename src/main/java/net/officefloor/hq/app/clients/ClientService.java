package net.officefloor.hq.app.clients;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
     * The visible clients (honouring the name search), each with how much they still owe, ordered the
     * way the list asks for: {@code "name"} alphabetically (case-insensitive), {@code "outstanding"}
     * by most owed first, anything else by the natural id order. The amount owed is computed the same
     * way as a client statement — each invoice's discounted total minus its payments, summed.
     */
    @Transactional(readOnly = true)
    public List<ClientView> listSorted(String query, String sort) {
        Map<Long, BigDecimal> owed = outstandingByClient();
        List<ClientView> views = new ArrayList<>();
        for (Client client : search(query)) {
            views.add(new ClientView(client.getId(), client.getName(), client.getEmail(),
                    client.getCurrency(), owed.getOrDefault(client.getId(), BigDecimal.ZERO)));
        }
        if ("name".equals(sort)) {
            views.sort(Comparator.comparing(ClientView::getName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(ClientView::getId));
        } else if ("outstanding".equals(sort)) {
            views.sort(Comparator.comparing(ClientView::getOutstanding).reversed()
                    .thenComparing(ClientView::getId));
        }
        return views;
    }

    /** What each client still owes across all their projects' invoices, keyed by client id. */
    private Map<Long, BigDecimal> outstandingByClient() {
        Map<Long, BigDecimal> owed = new HashMap<>();
        jdbc.query(
                "SELECT p.client_id AS client_id, "
                        + "SUM(i.amount * (1 - i.discount_pct / 100) "
                        + "- COALESCE((SELECT SUM(pay.amount) FROM payments pay "
                        + "WHERE pay.invoice_id = i.id), 0)) AS owed "
                        + "FROM invoices i JOIN projects p ON i.project_id = p.id "
                        + "GROUP BY p.client_id",
                rs -> {
                    BigDecimal value = rs.getBigDecimal("owed");
                    owed.put(rs.getLong("client_id"), value == null ? BigDecimal.ZERO : value);
                });
        return owed;
    }

    /** The clients tucked away (archived), oldest first — the "show archived" view's contents. */
    @Transactional(readOnly = true)
    public List<ClientView> listArchived() {
        return repository.findByArchivedTrueOrderByIdAsc().stream()
                .map(c -> new ClientView(c.getId(), c.getName(), c.getEmail(), c.getCurrency()))
                .toList();
    }

    /** One client with the currency they are paid in — what the client detail view reads. */
    @Transactional(readOnly = true)
    public ClientView get(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("A client id is required");
        }
        Client client = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No such client: " + id));
        return new ClientView(client.getId(), client.getName(), client.getEmail(),
                client.getCurrency());
    }

    /**
     * Set the currency a client is paid in (e.g. USD, EUR). Their money is shown in it everywhere.
     * Returns the updated client.
     */
    @Transactional
    public ClientView setCurrency(Long id, String currency) {
        if (id == null) {
            throw new IllegalArgumentException("A client id is required to set its currency");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("A currency is required");
        }
        Client client = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No such client: " + id));
        client.setCurrency(currency.trim());
        Client saved = repository.save(client);
        return new ClientView(saved.getId(), saved.getName(), saved.getEmail(), saved.getCurrency());
    }

    /**
     * Bring an archived client back: clear the flag so they return to the main list and search.
     * Records an audit entry so the action can be checked later, and returns the clients that remain
     * archived.
     */
    @Transactional
    public List<ClientView> restore(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("A client id is required to restore");
        }
        jdbc.update("UPDATE clients SET archived = FALSE WHERE id = ?", id);
        audit.record("CLIENT_RESTORED id=" + id);
        return listArchived();
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
        // The amount owed on an invoice is its discounted total — the percentage discount comes off
        // the subtotal before anything is owed — so both the per-invoice amount and what is still due
        // (that discounted total minus payments) reflect the discount, matching the invoice and
        // dashboard views.
        List<ClientStatementInvoiceView> invoices = jdbc.query(
                "SELECT i.id, p.id AS project_id, p.name AS project_name, "
                        + "i.amount * (1 - i.discount_pct / 100) AS net_amount, i.status, "
                        + "i.amount * (1 - i.discount_pct / 100) "
                        + "- COALESCE((SELECT SUM(pay.amount) FROM payments pay "
                        + "WHERE pay.invoice_id = i.id), 0) AS due_amount "
                        + "FROM invoices i JOIN projects p ON i.project_id = p.id "
                        + "WHERE p.client_id = ? ORDER BY p.id ASC, i.id ASC",
                (rs, i) -> new ClientStatementInvoiceView(rs.getLong("id"), rs.getLong("project_id"),
                        rs.getString("project_name"), rs.getBigDecimal("net_amount"),
                        rs.getString("status"), rs.getBigDecimal("due_amount")),
                clientId);
        // Group the invoices under the job (project) they were raised against, keeping the ordered
        // first-seen project order, so the statement can show each job with a subtotal of what is
        // still due on it. The subtotals sum to the outstanding total below.
        Map<Long, List<ClientStatementInvoiceView>> byProject = new LinkedHashMap<>();
        for (ClientStatementInvoiceView invoice : invoices) {
            byProject.computeIfAbsent(invoice.getProjectId(), k -> new ArrayList<>()).add(invoice);
        }
        List<ClientStatementProjectView> projects = new ArrayList<>();
        for (List<ClientStatementInvoiceView> group : byProject.values()) {
            BigDecimal subtotal = group.stream().map(ClientStatementInvoiceView::getDue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            ClientStatementInvoiceView first = group.get(0);
            projects.add(new ClientStatementProjectView(first.getProjectId(), first.getProjectName(),
                    subtotal, group));
        }
        BigDecimal outstanding = invoices.stream().map(ClientStatementInvoiceView::getDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // The whole statement is shown in the client's own currency.
        String currency = jdbc.queryForObject(
                "SELECT currency FROM clients WHERE id = ?", String.class, clientId);
        return new ClientStatementView(invoices, projects, outstanding,
                currency == null ? "USD" : currency);
    }

    /** The contacts kept for a client, oldest first. */
    @Transactional(readOnly = true)
    public List<ClientContactView> contactsFor(Long clientId) {
        return contacts.findByClientIdOrderByIdAsc(clientId).stream()
                .map(c -> new ClientContactView(c.getId(), c.getClientId(), c.getName(),
                        c.getEmail(), c.getRole(), c.isPrimary()))
                .toList();
    }

    /**
     * Pick a client's one main (primary) contact. Clears the flag across the client's contacts and
     * sets it on the chosen one, so exactly one is primary at a time. Returns the client's contacts.
     */
    @Transactional
    public List<ClientContactView> setPrimaryContact(Long clientId, Long contactId) {
        if (clientId == null || contactId == null) {
            throw new IllegalArgumentException("A client and contact are required");
        }
        jdbc.update("UPDATE contacts SET is_primary = FALSE WHERE client_id = ?", clientId);
        jdbc.update("UPDATE contacts SET is_primary = TRUE WHERE id = ? AND client_id = ?",
                contactId, clientId);
        return contactsFor(clientId);
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
                saved.getEmail(), saved.getRole(), saved.isPrimary());
    }

    /**
     * Correct a client's name and/or email. Validates the email like the create path and keeps the
     * no-two-clients-share-an-email rule (allowing the client to keep its own email). Records an
     * audit entry so the correction can be checked later, and returns the updated row.
     */
    @Transactional
    public Client update(Long id, String name, String email) {
        if (id == null) {
            throw new IllegalArgumentException("A client id is required to update");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A client requires a name");
        }
        if (email == null || !EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("A client requires a valid email address");
        }
        Client client = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No such client: " + id));
        // Two clients can never share an email (also enforced by the clients_email_unique
        // constraint); reject the duplicate before saving so the UI can flag it. The client may
        // keep its own existing email.
        if (repository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateClientEmailException(email);
        }
        client.setName(name);
        client.setEmail(email);
        Client saved = repository.save(client);
        audit.record("CLIENT_UPDATED id=" + id);
        return saved;
    }

    @Transactional
    public Client create(String name, String email) {
        if (email == null || !EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("A client requires a valid email address");
        }
        // Two clients can never share an email (also enforced by the clients_email_unique
        // constraint); reject the duplicate before saving so the UI can flag it.
        if (repository.existsByEmail(email)) {
            throw new DuplicateClientEmailException(email);
        }
        Client client = new Client();
        client.setName(name);
        client.setEmail(email);
        return repository.save(client);
    }
}
