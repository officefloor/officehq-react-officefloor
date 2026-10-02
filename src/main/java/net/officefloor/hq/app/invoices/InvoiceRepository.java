package net.officefloor.hq.app.invoices;

import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Invoice}. */
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
}
