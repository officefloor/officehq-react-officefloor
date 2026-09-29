package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * The money still owed in one currency, on the home dashboard. Clients are paid in different
 * currencies, which are never added together, so the outstanding total is broken out one row per
 * currency ({@code currency} + {@code amount}). Serialised as part of GET /api/dashboard.
 */
public record OutstandingByCurrency(String currency, BigDecimal amount) {
}
