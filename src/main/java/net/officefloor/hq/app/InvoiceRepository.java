package net.officefloor.hq.app;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Invoice}. A Spring bean, injected into the OfficeFloor logic classes. */
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /** A project's invoices, oldest first, so the list is stable across loads. */
    List<Invoice> findByProjectIdOrderByIdAsc(Long projectId);
}
