package net.officefloor.hq.app.clients;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Client}. */
public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findAllByOrderByIdAsc();
}
