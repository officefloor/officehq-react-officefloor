package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * A line item as the UI shows it: its id, the owning invoice's id, the description, the quantity, the
 * unit that quantity is measured in and the unit price. The UI works out each line's amount and the
 * invoice amount from these.
 */
public class LineItemView {

    private final Long id;
    private final Long invoiceId;
    private final String description;
    private final int quantity;
    private final String unit;
    private final BigDecimal unitPrice;

    public LineItemView(Long id, Long invoiceId, String description, int quantity, String unit,
            BigDecimal unitPrice) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.description = description;
        this.quantity = quantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
    }

    public Long getId() {
        return id;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
}
