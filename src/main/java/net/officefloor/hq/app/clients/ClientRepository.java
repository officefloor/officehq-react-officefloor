package net.officefloor.hq.app.clients;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Client}. */
public interface ClientRepository extends JpaRepository<Client, Long> {

    // Archived clients are tucked away, so the list and search only surface the non-archived ones.
    List<Client> findByArchivedFalseOrderByIdAsc();

    List<Client> findByArchivedFalseAndNameContainingIgnoreCaseOrderByIdAsc(String name);

    // Two clients can never share an email — the guard the create path checks before saving.
    boolean existsByEmail(String email);

    // The same guard for an edit: another client (not this one) must not already hold the email.
    boolean existsByEmailAndIdNot(String email, Long id);
}
