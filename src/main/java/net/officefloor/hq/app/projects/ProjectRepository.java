package net.officefloor.hq.app.projects;

import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Project}. */
public interface ProjectRepository extends JpaRepository<Project, Long> {
}
