package net.officefloor.hq.app.projects;

import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Project}. */
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /** Whether any project already carries the given reference code (codes are unique). */
    boolean existsByCode(String code);
}
