-- Invoices gain a terminal VOID state: an invoice sent by mistake can be cancelled, after which it
-- reads VOID and no longer counts toward what the owner is owed (only SENT invoices do). The status
-- column (V5/V9) already stores free-form lifecycle values; document VOID as a recognised state.
-- Additive metadata change on the existing invoice table; existing rows are unaffected.
COMMENT ON COLUMN invoice.status IS 'Lifecycle: DRAFT -> SENT -> PAID; or VOID when cancelled';
