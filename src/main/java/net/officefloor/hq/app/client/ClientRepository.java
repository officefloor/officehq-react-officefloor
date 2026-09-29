package net.officefloor.hq.app.client;

import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Client}. */
public interface ClientRepository extends JpaRepository<Client, Long> {
}
