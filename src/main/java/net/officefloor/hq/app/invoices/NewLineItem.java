package net.officefloor.hq.app.invoices;

import java.math.BigDecimal;
import net.officefloor.web.HttpObject;

/**
 * JSON request body for adding a line item to an invoice: what is being charged for, how many, and
 * the price for a single unit. {@link HttpObject} loads it from the request entity.
 */
@HttpObject
public class NewLineItem {

    private String description;
    private Integer qty;
    private String unit;
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
