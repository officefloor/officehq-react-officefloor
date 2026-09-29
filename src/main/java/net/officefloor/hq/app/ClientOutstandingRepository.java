package net.officefloor.hq.app;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Read-only query giving how much each (non-archived) client still owes, so the clients list can be
 * sorted by amount owed. Owed is worked out the same way "due" is elsewhere ({@link InvoiceRepository}):
 * an invoice's line-item total, less any discount, plus tax, minus what has been paid. DRAFT invoices
 * (not yet issued) and VOID invoices (cancelled) do not count toward money owed (V24).
 */
@Repository
public class ClientOutstandingRepository {

    private final JdbcTemplate jdbc;

    public ClientOutstandingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // An invoice's amount is the sum of its line items (qty * unit_price); zero when it has none.
    private static final String AMOUNT_SUM =
            "COALESCE((SELECT SUM(li.qty * li.unit_price) FROM line_items li"
                    + " WHERE li.invoice_id = i.id), 0)";

    // What has been paid against the invoice: the sum of its payments (zero when none).
    private static final String PAID_SUM =
            "COALESCE((SELECT SUM(pm.amount) FROM payments pm"
                    + " WHERE pm.invoice_id = i.id), 0)";

    // The discount (V29) comes off the line-item total; tax (V30) is added on top of that, mirroring
    // how "due" is derived on individual invoices so the sort matches what each invoice reads as owed.
    private static final String TAXED_AMOUNT =
            "(" + AMOUNT_SUM + " * (100 - i.discount_pct) / 100 * (100 + i.tax_pct) / 100)";

    // What is still owed on the invoice: its taxed (post-discount) amount minus what has been paid.
    private static final String DUE = "(" + TAXED_AMOUNT + " - " + PAID_SUM + ")";

    /** Outstanding total per non-archived client (zero when a client owes nothing), ordered by id. */
    public List<ClientOutstanding> outstandingByClient() {
        return jdbc.query(
                "SELECT c.id AS client_id, COALESCE(SUM(" + DUE + "), 0) AS outstanding"
                        + " FROM clients c"
                        + " LEFT JOIN projects p ON p.client_id = c.id"
                        + " LEFT JOIN invoices i ON i.project_id = p.id"
                        + " AND i.status NOT IN ('DRAFT', 'VOID')"
                        + " WHERE c.archived = FALSE"
                        + " GROUP BY c.id ORDER BY c.id",
                (rs, n) -> new ClientOutstanding(
                        rs.getLong("client_id"), rs.getBigDecimal("outstanding")));
    }
}
