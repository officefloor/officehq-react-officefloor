-- Each client is paid in their own currency (e.g. USD, EUR). Their money is shown in that currency
-- everywhere it appears. Existing clients default to USD; the owner can change a client's currency.
ALTER TABLE client ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'USD';
