package net.officefloor.hq.app.invoice;

import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Payment}. */
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
