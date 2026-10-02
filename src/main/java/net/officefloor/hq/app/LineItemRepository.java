package net.officefloor.hq.app;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link LineItem}. A Spring bean, injected into the OfficeFloor logic classes. */
public interface LineItemRepository extends JpaRepository<LineItem, Long> {

    /** An invoice's line items, oldest first, so the list is stable across loads. */
    List<LineItem> findByInvoiceIdOrderByIdAsc(Long invoiceId);
}
