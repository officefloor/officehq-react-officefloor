package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * Works out an invoice's payment status from what has actually been paid against it, rather than the
 * owner flipping it by hand: PAID once payments cover the billed amount, PARTIAL once some (but not
 * all) is paid, otherwise the invoice's stored lifecycle status (e.g. a sent, unpaid invoice reads
 * SENT). Shared by the reads that surface an invoice's status.
 */
final class InvoiceStatus {

    private InvoiceStatus() {
    }

    static String derive(String storedStatus, BigDecimal amount, BigDecimal paid) {
        if (amount != null && amount.signum() > 0 && paid.compareTo(amount) >= 0) {
            return "PAID";
        }
        if (paid.signum() > 0) {
            return "PARTIAL";
        }
        return storedStatus;
    }
}
