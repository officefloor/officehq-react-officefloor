package net.officefloor.hq.app;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Project}. A Spring bean, injected into the OfficeFloor logic classes. */
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /** All projects, oldest first, so the list is stable across loads. */
    List<Project> findAllByOrderByIdAsc();

    /** The projects done for one client, oldest first, so the list is stable across loads. */
    List<Project> findByClientIdOrderByIdAsc(Long clientId);

    /** The active (not archived) projects for one client — archived ones drop off the client's list. */
    List<Project> findByClientIdAndArchivedFalseOrderByIdAsc(Long clientId);

    /** How many projects one client has — for the client's at-a-glance counts. */
    long countByClientId(Long clientId);
}
