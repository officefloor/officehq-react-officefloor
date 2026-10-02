package net.officefloor.hq.app.clients;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Contact}. */
public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findByClientIdOrderByIdAsc(Long clientId);
}
