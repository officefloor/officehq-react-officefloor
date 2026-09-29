package net.officefloor.hq.app;

import java.util.List;

/**
 * One page of the cross-project invoice list: the {@code invoices} on this page together with the
 * {@code total} number of matching invoices (across every page), the 1-based {@code page} number
 * and the {@code pageSize}. The UI uses {@code total}/{@code pageSize} to know when next/previous
 * are available. Serialised as JSON by GET /api/invoices/all.
 */
public record InvoicePage(List<InvoiceListing> invoices, long total, int page, int pageSize) {
}
