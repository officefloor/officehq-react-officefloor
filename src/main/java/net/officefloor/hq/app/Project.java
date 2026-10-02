package net.officefloor.hq.app;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** A project the owner does for a client: a name and the owning client's id. Maps to {@code project} (V3). */
@Entity
@Table(name = "project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "client_id")
    private Long clientId;

    private boolean archived;

    private String status;

    /** The budget set against this project; what has been invoiced is compared to it (V23). */
    private BigDecimal budget;

    public Project() {
    }

    public Project(String name, Long clientId) {
        this(name, clientId, "ACTIVE");
    }

    public Project(String name, Long clientId, String status) {
        this(name, clientId, status, BigDecimal.ZERO);
    }

    public Project(String name, Long clientId, String status, BigDecimal budget) {
        this.name = name;
        this.clientId = clientId;
        this.archived = false;
        this.status = status;
        this.budget = budget;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }
}
