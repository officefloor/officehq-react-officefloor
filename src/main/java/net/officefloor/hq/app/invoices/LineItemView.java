package net.officefloor.hq.app.invoices;

import java.math.BigDecimal;

/**
 * JSON response shape for a line item (what the UI renders): id, owning invoice, the description of
 * the thing being charged for, how many (quantity) and the price for a single unit. The UI derives
 * the line's amount and the invoice total from quantity times unit price.
 */
public class LineItemView {

    private final long id;
    private final long invoiceId;
    private final String description;
    private final int qty;
    private final String unit;
    private final BigDecimal unitPrice;

    public LineItemView(long id, long invoiceId, String description, int qty, String unit,
            BigDecimal unitPrice) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.description = description;
        this.qty = qty;
        this.unit = unit;
        this.unitPrice = unitPrice;
    }

    public long getId() {
        return id;
    }

    public long getInvoiceId() {
        return invoiceId;
    }

    public String getDescription() {
        return description;
    }

    public int getQty() {
        return qty;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
}
