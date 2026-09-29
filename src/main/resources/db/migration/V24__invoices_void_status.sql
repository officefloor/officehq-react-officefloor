-- An invoice sent by mistake can be cancelled (voided). VOID is a new terminal value of the stored
-- status column: once set it overrides the payment-derived status (the invoice reads VOID) and the
-- invoice stops counting toward money owed. The status column is an unconstrained VARCHAR(16), so
-- VOID fits without altering the column; this migration documents the added lifecycle state.
-- Additive: a new versioned migration alongside the existing invoices table.
COMMENT ON COLUMN invoices.status IS
    'Issued lifecycle: DRAFT (not sent), SENT (issued), or VOID (cancelled); PARTIAL/PAID are derived from payments.';
