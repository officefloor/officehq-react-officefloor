package net.officefloor.hq.app;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Contact}. A Spring bean, injected into the OfficeFloor logic classes. */
public interface ContactRepository extends JpaRepository<Contact, Long> {

    /** The contacts for one client, oldest first, so the list is stable across loads. */
    List<Contact> findByClientIdOrderByIdAsc(Long clientId);
}
