package net.officefloor.hq.app.invoice;

import java.math.BigDecimal;

/** Request body for adding a line item: what is being charged for, how many, and the price each. */
public class NewLineItem {

    private String description;

    private Integer qty;

    private BigDecimal unitPrice;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
