package net.officefloor.hq.app;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Project}. A Spring bean, injected into the OfficeFloor logic classes. */
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /** All projects, oldest first, so the list is stable across loads. */
    List<Project> findAllByOrderByIdAsc();
}
