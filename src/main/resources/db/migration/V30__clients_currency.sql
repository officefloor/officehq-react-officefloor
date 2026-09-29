-- Each client is paid in their own currency (e.g. USD, EUR). Their money is shown in that currency
-- everywhere it appears. Additive: a new column on the existing clients table. Existing rows (and
-- normal creates that omit it) default to USD (NOT NULL so the column always has a value to format
-- with). Stored as the ISO currency code (e.g. 'USD', 'EUR').
ALTER TABLE clients ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'USD';
