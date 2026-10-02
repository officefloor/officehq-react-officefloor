import React, { useEffect, useState } from 'react';

// A client's currency, rendered inside the clients feature when a client is opened: which currency
// the owner is paid in by this client. The owner picks it and saves it; from then on the client's
// money is shown in that currency everywhere. Owns its own state and data loading (no global store);
// composed, not branched.
type Client = { id: number; name: string; email: string; currency: string };

// The currencies the app can show, matching the server's supported set (SetClientCurrencyLogic).
const CURRENCIES = ['USD', 'EUR'];

export function ClientCurrency({ clientId }: { clientId: number }) {
  // The saved currency (what the client is actually paid in) and the pending dropdown choice.
  const [currency, setCurrency] = useState('USD');
  const [choice, setChoice] = useState('USD');

  useEffect(() => {
    let active = true;
    async function load() {
      const res = await fetch('/api/clients');
      const list: Client[] = await res.json();
      const client = list.find((c) => c.id === clientId);
      if (active && client) {
        setCurrency(client.currency);
        setChoice(client.currency);
      }
    }
    void load();
    return () => {
      active = false;
    };
  }, [clientId]);

  async function save() {
    const res = await fetch(`/api/clients/${clientId}/currency`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ currency: choice }),
    });
    if (!res.ok) {
      return;
    }
    const saved: Client = await res.json();
    setCurrency(saved.currency);
    setChoice(saved.currency);
  }

  return (
    <section data-testid="client-currency-section">
      <span data-testid="client-currency-label">Currency</span>
      <span data-testid="client-currency">{currency}</span>
      <select
        data-testid="client-currency-select"
        value={choice}
        onChange={(e) => setChoice(e.target.value)}
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
