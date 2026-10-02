package net.officefloor.hq.app;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Client}. A Spring bean, injected into the OfficeFloor logic classes. */
public interface ClientRepository extends JpaRepository<Client, Long> {

    /** All clients, oldest first, so the list is stable across loads. */
    List<Client> findAllByOrderByIdAsc();

    /** The active (not archived) clients, oldest first — archived ones drop off the list. */
    List<Client> findByArchivedFalseOrderByIdAsc();

    /** The archived (tucked-away) clients, oldest first — for the "show archived" view. */
    List<Client> findByArchivedTrueOrderByIdAsc();

    /** Active clients whose name contains the query (case-insensitive), oldest first — for search. */
    List<Client> findByArchivedFalseAndNameContainingIgnoreCaseOrderByIdAsc(String name);

    /** Whether any client already holds this email (case-insensitive) — emails must be unique. */
    boolean existsByEmailIgnoreCase(String email);

    /** Whether a DIFFERENT client already holds this email — for validating an edit of this client. */
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
