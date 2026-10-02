package net.officefloor.hq.app;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Task}. A Spring bean, injected into the OfficeFloor logic classes. */
public interface TaskRepository extends JpaRepository<Task, Long> {

    /** A project's tasks, oldest first, so the checklist is stable across loads. */
    List<Task> findByProjectIdOrderByIdAsc(Long projectId);

    /** A project's tasks in one state (done = true/false), oldest first. */
    List<Task> findByProjectIdAndDoneOrderByIdAsc(Long projectId, boolean done);
}
