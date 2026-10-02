package net.officefloor.hq.app;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Note}. A Spring bean, injected into the OfficeFloor logic classes. */
public interface NoteRepository extends JpaRepository<Note, Long> {

    /** A target's notes, newest first (ties broken by id so ordering is stable across loads). */
    List<Note> findByTargetTypeAndTargetIdOrderByAtDescIdDesc(String targetType, Long targetId);
}
