package net.officefloor.hq.app.client;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Client}. */
public interface ClientRepository extends JpaRepository<Client, Long> {

    /** Clients still in play — archived ones are tucked away and excluded. */
    List<Client> findByArchivedFalse();
}
