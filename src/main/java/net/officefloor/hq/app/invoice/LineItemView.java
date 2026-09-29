package net.officefloor.hq.app.invoice;

import java.math.BigDecimal;

/** A line item as the invoice detail surfaces it: what it is, how many, and the price of each. */
public class LineItemView {

    private final Long id;
    private final Long invoiceId;
    private final String description;
    private final Integer qty;
    private final String unit;
    private final BigDecimal unitPrice;

    public LineItemView(Long id, Long invoiceId, String description, Integer qty, String unit,
            BigDecimal unitPrice) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.description = description;
        this.qty = qty;
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

    public Integer getQty() {
        return qty;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
}
