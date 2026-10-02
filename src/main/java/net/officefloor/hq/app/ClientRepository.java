package net.officefloor.hq.app;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Client}. A Spring bean, injected into the OfficeFloor logic classes. */
public interface ClientRepository extends JpaRepository<Client, Long> {

    /** All clients, oldest first, so the list is stable across loads. */
    List<Client> findAllByOrderByIdAsc();

    /** The active (not archived) clients, oldest first — archived ones drop off the list. */
    List<Client> findByArchivedFalseOrderByIdAsc();
}
