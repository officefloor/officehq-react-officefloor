package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** Data access for {@link Invoice} rows (JdbcTemplate over the schema in V4__invoices.sql). */
@Repository
public class InvoiceRepository {

    private final JdbcTemplate jdbc;

    public InvoiceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final org.springframework.jdbc.core.RowMapper<Invoice> MAPPER =
            (rs, i) -> new Invoice(rs.getLong("id"), rs.getLong("project_id"),
                    rs.getBigDecimal("amount"), rs.getString("status"),
                    rs.getString("issued_date"), rs.getString("due_date"),
                    rs.getBigDecimal("due"));

    // An invoice's amount is the sum of its line items (qty * unit_price); a line-item-less invoice
    // totals zero. Derived here so the amount always reflects the current line items.
    private static final String AMOUNT_SUM =
            "COALESCE((SELECT SUM(li.qty * li.unit_price) FROM line_items li"
                    + " WHERE li.invoice_id = i.id), 0)";

    // What has been paid against the invoice: the sum of its payments (zero when none). The amount
    // still due is the invoice amount minus this.
    private static final String PAID_SUM =
            "COALESCE((SELECT SUM(pm.amount) FROM payments pm"
                    + " WHERE pm.invoice_id = i.id), 0)";

    private static final String DUE = "(" + AMOUNT_SUM + " - " + PAID_SUM + ")";

    // An invoice's status is worked out from its recorded payments, not flipped by hand: it stays
    // DRAFT until issued, reads SENT once issued but unpaid, PARTIAL once some (but not all) of the
    // amount is paid, and PAID once the payments cover the amount. Derived here so the status always
    // reflects the payments on file.
    private static final String DERIVED_STATUS =
            "CASE WHEN i.status = 'DRAFT' THEN 'DRAFT'"
                    + " WHEN " + AMOUNT_SUM + " > 0 AND " + PAID_SUM + " >= " + AMOUNT_SUM
                    + " THEN 'PAID'"
                    + " WHEN " + PAID_SUM + " > 0 THEN 'PARTIAL'"
                    + " ELSE 'SENT' END";

    public List<Invoice> findByProject(long projectId) {
        return jdbc.query(
                "SELECT i.id, i.project_id, " + AMOUNT_SUM + " AS amount, " + DUE + " AS due,"
                        + " " + DERIVED_STATUS + " AS status, i.issued_date, i.due_date"
                        + " FROM invoices i WHERE i.project_id = ? ORDER BY i.id",
                MAPPER, projectId);
    }

    /** A project's invoices ordered by due date, earliest first (nulls last, id as tiebreak). */
    public List<Invoice> findByProjectOrderByDueDate(long projectId) {
        return jdbc.query(
                "SELECT i.id, i.project_id, " + AMOUNT_SUM + " AS amount, " + DUE + " AS due,"
                        + " " + DERIVED_STATUS + " AS status, i.issued_date, i.due_date"
                        + " FROM invoices i WHERE i.project_id = ?"
                        + " ORDER BY i.due_date ASC NULLS LAST, i.id",
                MAPPER, projectId);
    }

    private static final org.springframework.jdbc.core.RowMapper<InvoiceListing> LISTING_MAPPER =
            (rs, i) -> new InvoiceListing(rs.getLong("id"), rs.getLong("project_id"),
                    rs.getString("project_name"), rs.getBigDecimal("amount"),
                    rs.getString("status"));

    // Every invoice joined to its project name, ordered by id. The derived status is an expression,
    // so filtering by stage wraps this as a subquery and filters on the aliased column.
    private static final String LISTING_SELECT =
            "SELECT i.id AS id, i.project_id AS project_id, p.name AS project_name, "
                    + AMOUNT_SUM + " AS amount, " + DERIVED_STATUS + " AS status FROM invoices i"
                    + " JOIN projects p ON p.id = i.project_id";

    /** Every invoice across all projects, joined to its project name, ordered by id. */
    public List<InvoiceListing> findAllWithProject() {
        return jdbc.query(LISTING_SELECT + " ORDER BY id", LISTING_MAPPER);
    }

    /**
     * One page of the cross-project invoice list, ordered by id. {@code status} narrows to a single
     * stage (DRAFT/SENT/PARTIAL/PAID) when non-null/blank; otherwise every stage is included.
     */
    public List<InvoiceListing> findPage(String status, int limit, int offset) {
        if (status == null || status.isBlank()) {
            return jdbc.query(LISTING_SELECT + " ORDER BY id LIMIT ? OFFSET ?",
                    LISTING_MAPPER, limit, offset);
        }
        return jdbc.query(
                "SELECT id, project_id, project_name, amount, status FROM (" + LISTING_SELECT
                        + ") t WHERE t.status = ? ORDER BY id LIMIT ? OFFSET ?",
                LISTING_MAPPER, status, limit, offset);
    }

    /** How many invoices match the (optional) stage filter, across every page. */
    public long countAll(String status) {
        if (status == null || status.isBlank()) {
            return jdbc.queryForObject("SELECT COUNT(*) FROM invoices", Long.class);
        }
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM (" + LISTING_SELECT + ") t WHERE t.status = ?",
                Long.class, status);
    }

    public Invoice findById(long id) {
        List<Invoice> found = jdbc.query(
                "SELECT i.id, i.project_id, " + AMOUNT_SUM + " AS amount, " + DUE + " AS due,"
                        + " " + DERIVED_STATUS + " AS status, i.issued_date, i.due_date"
                        + " FROM invoices i WHERE i.id = ?",
                MAPPER, id);
        return found.isEmpty() ? null : found.get(0);
    }

    public Invoice create(long projectId, BigDecimal amount) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO invoices (project_id, amount) VALUES (?, ?)", new String[] {"id"});
            ps.setLong(1, projectId);
            ps.setBigDecimal(2, amount);
            return ps;
        }, keys);
        // A freshly created invoice has no payments yet, so the whole amount is still due.
        return new Invoice(keys.getKey().longValue(), projectId, amount, "DRAFT", null, null,
                amount);
    }

    /** Flip an invoice from DRAFT to SENT and return the updated row (null if no such invoice). */
    public Invoice markSent(long id) {
        jdbc.update("UPDATE invoices SET status = 'SENT' WHERE id = ?", id);
        return findById(id);
    }

    /** Flip an invoice to PAID and return the updated row (null if no such invoice). */
    public Invoice markPaid(long id) {
        jdbc.update("UPDATE invoices SET status = 'PAID' WHERE id = ?", id);
        return findById(id);
    }
}
