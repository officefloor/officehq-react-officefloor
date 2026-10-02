package net.officefloor.hq.app;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * The dashboard's reference ("as of") date, used to decide which SENT invoices are overdue. A single
 * configuration row (id = 1); absent in a real deploy, where the dashboard uses the current date.
 * Maps to {@code dashboard_config} (V24).
 */
@Entity
@Table(name = "dashboard_config")
public class DashboardConfig {

    @Id
    private Long id;

    @Column(name = "as_of")
    private LocalDate asOf;

    public DashboardConfig() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getAsOf() {
        return asOf;
    }

    public void setAsOf(LocalDate asOf) {
        this.asOf = asOf;
    }
}
