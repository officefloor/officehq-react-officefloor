-- An invoice can carry a percentage sales tax added ON TOP of its total, worked out AFTER the
-- discount: the taxable base is the subtotal less the discount, and the tax is that percentage of
-- it. The invoice's amount therefore becomes (subtotal - discount) plus that tax. Additive: a new
-- column on the existing invoices table. Existing invoices carry no tax, so default to 0 (NOT NULL
-- so the column always has a value to compute with). Stored as a whole/fractional percent
-- (e.g. 20 = 20%).
ALTER TABLE invoices ADD COLUMN tax_pct DECIMAL(5, 2) NOT NULL DEFAULT 0;
