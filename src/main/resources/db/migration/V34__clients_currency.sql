-- Each client is paid in their own currency; their money is shown in it everywhere. Existing
-- clients keep the app's original currency (USD) until the owner sets another.
ALTER TABLE clients ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'USD';
