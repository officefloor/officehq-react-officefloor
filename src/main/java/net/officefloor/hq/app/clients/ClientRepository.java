package net.officefloor.hq.app.clients;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Client}. */
public interface ClientRepository extends JpaRepository<Client, Long> {

    // Archived clients are tucked away, so the list and search only surface the non-archived ones.
    List<Client> findByArchivedFalseOrderByIdAsc();

    List<Client> findByArchivedFalseAndNameContainingIgnoreCaseOrderByIdAsc(String name);
}
