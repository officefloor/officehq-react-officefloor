package net.officefloor.hq.app.project;

import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Project}. */
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /** Whether a project already uses this reference code — two projects cannot share one. */
    boolean existsByCode(String code);
}
