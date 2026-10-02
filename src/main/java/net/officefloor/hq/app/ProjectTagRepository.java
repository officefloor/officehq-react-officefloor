package net.officefloor.hq.app;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link ProjectTag}. A Spring bean, injected into the OfficeFloor logic classes. */
public interface ProjectTagRepository extends JpaRepository<ProjectTag, Long> {

    /** A project's tag links, oldest first, so a project's chips are stable across loads. */
    List<ProjectTag> findByProjectIdOrderByTagIdAsc(Long projectId);

    /** Whether a project already carries a tag (so we never add it twice). */
    boolean existsByProjectIdAndTagId(Long projectId, Long tagId);

    /** The link rows joining a project to a tag (one, by the UNIQUE constraint), for removal by id. */
    List<ProjectTag> findByProjectIdAndTagId(Long projectId, Long tagId);
}
