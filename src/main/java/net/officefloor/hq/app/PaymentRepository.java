package net.officefloor.hq.app;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Payment}. A Spring bean, injected into the OfficeFloor logic classes. */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /** An invoice's payments, oldest first, so the list is stable across loads. */
    List<Payment> findByInvoiceIdOrderByIdAsc(Long invoiceId);
}
