package net.officefloor.hq.app.note;

import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Note}. */
public interface NoteRepository extends JpaRepository<Note, Long> {
}
