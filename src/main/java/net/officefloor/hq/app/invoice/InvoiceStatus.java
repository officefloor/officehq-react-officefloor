package net.officefloor.hq.app.invoice;

import java.math.BigDecimal;

/**
 * Works out an invoice's payment status from what has actually been paid against it, rather than a
 * flag flipped by hand: once some (but not all) of the amount is covered it reads PARTIAL, and once
 * the payments cover the whole amount it reads PAID. With nothing paid yet the invoice keeps its
 * stored lifecycle status (DRAFT before it is sent, SENT after).
 */
final class InvoiceStatus {

    private InvoiceStatus() {
    }

    /**
     * @param stored the invoice's own lifecycle status (e.g. DRAFT / SENT).
     * @param amount the invoice total (sum of its line items).
     * @param paid   the total of the payments recorded against it.
     */
    static String derive(String stored, BigDecimal amount, BigDecimal paid) {
        if (paid == null || paid.signum() <= 0) {
            return stored;
        }
        if (amount != null && amount.signum() > 0 && paid.compareTo(amount) >= 0) {
            return "PAID";
        }
        return "PARTIAL";
    }
}
