package net.officefloor.hq.app;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A single lump sum a client paid at once, to be split across several of that client's invoices: the
 * owning client's id, how much was paid and the date it was paid. Each share of the lump is an
 * ordinary {@link Payment} that points back here. Maps to {@code client_payment} (V34).
 */
@Entity
@Table(name = "client_payment")
public class ClientPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id")
    private Long clientId;

    private BigDecimal amount;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    public ClientPayment() {
    }

    public ClientPayment(Long clientId, BigDecimal amount, LocalDate paidDate) {
        this.clientId = clientId;
        this.amount = amount;
        this.paidDate = paidDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
    }
}
