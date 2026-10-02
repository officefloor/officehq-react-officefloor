-- A client sometimes pays one lump sum that is split across several of their invoices. Each share is
-- still recorded as its own payment row (so every invoice's balance stays correct on its own), but
-- the shares that came from the same lump sum now carry a shared reference so they can be recognised
-- as one payment. Existing payments predate this and keep a NULL reference (they stand on their own).
ALTER TABLE payments ADD COLUMN batch_ref VARCHAR(64);
