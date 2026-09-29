package net.officefloor.hq.app.task;

import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Task}. */
public interface TaskRepository extends JpaRepository<Task, Long> {
}
