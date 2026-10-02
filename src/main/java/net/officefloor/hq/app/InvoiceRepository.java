package net.officefloor.hq.app;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Invoice}. A Spring bean, injected into the OfficeFloor logic classes. */
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /** Every invoice across all projects, oldest first, so the list is stable across loads. */
    List<Invoice> findAllByOrderByIdAsc();

    /** A project's invoices, oldest first, so the list is stable across loads. */
    List<Invoice> findByProjectIdOrderByIdAsc(Long projectId);

    /** A project's invoices, earliest due date first (id as a stable tie-breaker). */
    List<Invoice> findByProjectIdOrderByDueDateAscIdAsc(Long projectId);

    /** Every invoice with the given status (e.g. SENT), oldest first — used to total what is owed. */
    List<Invoice> findByStatusOrderByIdAsc(String status);

    /**
     * How many invoices with the given status fell due before {@code date} — i.e. are overdue as of
     * that reference date (strictly before, so an invoice due on the date itself is not yet overdue).
     */
    long countByStatusAndDueDateBefore(String status, LocalDate date);
}
