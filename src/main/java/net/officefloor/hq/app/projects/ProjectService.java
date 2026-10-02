package net.officefloor.hq.app.projects;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for projects. The list view joins each project to its client to surface the
 * client's NAME (the UI shows names, not ids). The join reads the {@code clients} table via SQL so
 * the feature stays self-contained and does not import the clients feature's Java types.
 */
@Service
public class ProjectService {

    private final ProjectRepository repository;
    private final JdbcTemplate jdbc;

    public ProjectService(ProjectRepository repository, JdbcTemplate jdbc) {
        this.repository = repository;
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public List<ProjectView> list() {
        return jdbc.query(
                "SELECT p.id, p.name, p.client_id, c.name AS client_name "
                        + "FROM projects p JOIN clients c ON p.client_id = c.id "
                        + "ORDER BY p.id ASC",
                (rs, i) -> new ProjectView(rs.getLong("id"), rs.getString("name"),
                        rs.getLong("client_id"), rs.getString("client_name")));
    }

    @Transactional
    public ProjectView create(String name, Long clientId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A project requires a name");
        }
        if (clientId == null) {
            throw new IllegalArgumentException("A project requires a client");
        }
        Project project = new Project();
        project.setName(name);
        project.setClientId(clientId);
        Project saved = repository.save(project);
        String clientName = jdbc.queryForObject(
                "SELECT name FROM clients WHERE id = ?", String.class, clientId);
        return new ProjectView(saved.getId(), saved.getName(), clientId, clientName);
    }
}
