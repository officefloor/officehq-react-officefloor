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
 * A payment a client has made against an invoice: the owning invoice's id, how much was paid and the
 * date it was paid. Maps to {@code payment} (V19).
 */
@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id")
    private Long invoiceId;

    private BigDecimal amount;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    // The lump {@link ClientPayment} this payment is a share of, or null for a standalone payment
    // recorded directly against a single invoice (V34).
    @Column(name = "client_payment_id")
    private Long clientPaymentId;

    public Payment() {
    }

    public Payment(Long invoiceId, BigDecimal amount, LocalDate paidDate) {
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.paidDate = paidDate;
    }

    public Payment(Long invoiceId, BigDecimal amount, LocalDate paidDate, Long clientPaymentId) {
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.paidDate = paidDate;
        this.clientPaymentId = clientPaymentId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
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

    public Long getClientPaymentId() {
        return clientPaymentId;
    }

    public void setClientPaymentId(Long clientPaymentId) {
        this.clientPaymentId = clientPaymentId;
    }
}
