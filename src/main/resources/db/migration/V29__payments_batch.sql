-- Splitting one lump-sum client payment across several invoices records one payment row per invoice,
-- all sharing a batch reference so the allocations of a single lump sum can be told apart from other
-- payments. Additive: a nullable column on payments; a plain single payment simply leaves it null.
ALTER TABLE payments ADD COLUMN batch_ref BIGINT;
