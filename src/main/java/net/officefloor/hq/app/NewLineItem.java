package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * Request body for adding a line item to an invoice: what is being charged for (description), how
 * many (quantity), the unit that quantity is measured in (unit) and the price of each (unitPrice).
 */
public class NewLineItem {

    private String description;

    private Integer quantity;

    private String unit;

    private BigDecimal unitPrice;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
