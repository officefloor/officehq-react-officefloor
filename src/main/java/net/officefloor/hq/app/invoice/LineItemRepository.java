package net.officefloor.hq.app.invoice;

import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link LineItem}. */
public interface LineItemRepository extends JpaRepository<LineItem, Long> {
}
