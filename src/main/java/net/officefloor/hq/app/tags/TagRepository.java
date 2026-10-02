package net.officefloor.hq.app.tags;

import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Tag}. */
public interface TagRepository extends JpaRepository<Tag, Long> {
}
