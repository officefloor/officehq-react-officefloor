-- Different clients are paid in different currencies. Each client carries their own currency code
-- (e.g. USD, EUR); their money is shown in it everywhere. Defaults to USD so every existing client
-- keeps a sensible currency until it is set.
ALTER TABLE clients ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'USD';
