package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * One line of a client's statement: an invoice of theirs (across any of their projects) with its
 * total {@code amount}, how much is still {@code due} on it, and its current {@code status}.
 * Serialised as JSON by GET /api/clients/statement.
 */
public record StatementInvoice(long id, long projectId, String projectName, BigDecimal amount,
        BigDecimal due, String status) {
}
