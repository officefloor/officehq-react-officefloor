import React, { useEffect, useRef, useState } from 'react';

// Set the currency a client is paid in. Their money is shown in this currency everywhere (their
// invoices, their statement, and the home screen totals). Owns its own state (CLAUDE.md — features
// own their state) and reads/writes the dedicated /api/clients/currency endpoint.
const CURRENCIES = ['USD', 'EUR'];

export function ClientCurrency({ clientId }: { clientId: number }) {
  const [currency, setCurrency] = useState('USD');
  const [selected, setSelected] = useState('USD');
  // Once the owner picks or saves a currency, a slow initial GET must not clobber their choice, so
  // the load only fills in the current value while the panel is still untouched.
  const touched = useRef(false);

  useEffect(() => {
    let active = true;
    async function load() {
      const res = await fetch(`/api/clients/currency?clientId=${clientId}`);
      if (res.ok && active && !touched.current) {
        const data: { currency: string } = await res.json();
        setCurrency(data.currency);
        setSelected(data.currency);
      }
    }
    void load();
    return () => {
      active = false;
    };
  }, [clientId]);

  async function save() {
    touched.current = true;
    const res = await fetch('/api/clients/currency', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id: clientId, currency: selected }),
    });
    if (res.ok) {
      const data: { currency: string } = await res.json();
      setCurrency(data.currency);
      setSelected(data.currency);
    }
  }

  return (
    <section data-testid="client-currency-section">
      <p>
        Currency: <span data-testid="client-currency">{currency}</span>
      </p>
      <select
        data-testid="client-currency-select"
        value={selected}
        onChange={(e) => {
          touched.current = true;
          setSelected(e.target.value);
        }}
      >
        {CURRENCIES.map((c) => (
          <option key={c} value={c}>
            {c}
          </option>
        ))}
      </select>
      <button type="button" data-testid="client-currency-save" onClick={() => void save()}>
        Save currency
      </button>
    </section>
  );
}
