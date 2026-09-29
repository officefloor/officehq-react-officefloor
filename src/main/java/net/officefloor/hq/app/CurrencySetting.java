package net.officefloor.hq.app;

/**
 * A client's currency setting: the client and the ISO currency code (e.g. {@code USD}, {@code EUR})
 * their money is shown in. Serialised as JSON by GET/POST /api/clients/currency.
 */
public record CurrencySetting(long clientId, String currency) {
}
