package net.officefloor.hq.app;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Tag}. A Spring bean, injected into the OfficeFloor logic classes. */
public interface TagRepository extends JpaRepository<Tag, Long> {

    /** Every tag, by name order, so the add-a-tag picker is stable across loads. */
    List<Tag> findAllByOrderByNameAsc();
}
