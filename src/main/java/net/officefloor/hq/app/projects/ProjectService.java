package net.officefloor.hq.app.projects;

import java.util.List;
import net.officefloor.hq.app.Audit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for projects. The list view joins each project to its client to surface the
 * client's NAME (the UI shows names, not ids). The join reads the {@code clients} table via SQL so
 * the feature stays self-contained and does not import the clients feature's Java types. Deleting a
 * project removes its row and records an audit entry so the action can be checked later. Archiving a
 * project keeps the row but tucks it away: it drops off the default list and reappears only when the
 * caller asks to include archived ones.
 */
@Service
public class ProjectService {

    private final ProjectRepository repository;
    private final JdbcTemplate jdbc;
    private final Audit audit;

    public ProjectService(ProjectRepository repository, JdbcTemplate jdbc, Audit audit) {
        this.repository = repository;
        this.jdbc = jdbc;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<ProjectView> list() {
        return list(false);
    }

    /**
     * List projects with their client's name. Archived projects are tucked away: they are omitted
     * unless {@code includeArchived} is true (how the UI's "show archived" toggle reveals them).
     */
    @Transactional(readOnly = true)
    public List<ProjectView> list(boolean includeArchived) {
        return list(includeArchived, null);
    }

    /**
     * List projects with their client's name, optionally narrowed to those carrying a given tag (the
     * UI's "filter by tag" picker). A null {@code tagId} means no tag filter — list every project the
     * archived flag allows. The join to {@code project_tags} reads the tag link via SQL so the feature
     * stays self-contained and does not import the tags feature's Java types.
     */
    @Transactional(readOnly = true)
    public List<ProjectView> list(boolean includeArchived, Long tagId) {
        StringBuilder sql = new StringBuilder(
                "SELECT p.id, p.name, p.client_id, p.status, c.name AS client_name "
                        + "FROM projects p JOIN clients c ON p.client_id = c.id ");
        List<Object> args = new java.util.ArrayList<>();
        if (tagId != null) {
            sql.append("JOIN project_tags pt ON pt.project_id = p.id AND pt.tag_id = ? ");
            args.add(tagId);
        }
        if (!includeArchived) {
            sql.append("WHERE p.archived = FALSE ");
        }
        sql.append("ORDER BY p.id ASC");
        return jdbc.query(sql.toString(),
                (rs, i) -> new ProjectView(rs.getLong("id"), rs.getString("name"),
                        rs.getLong("client_id"), rs.getString("client_name"),
                        rs.getString("status")),
                args.toArray());
    }

    @Transactional
    public ProjectView create(String name, Long clientId, String status) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A project requires a name");
        }
        if (clientId == null) {
            throw new IllegalArgumentException("A project requires a client");
        }
        // Default to ACTIVE when the caller doesn't pick a status, matching the column default.
        String projectStatus = (status == null || status.isBlank()) ? "ACTIVE" : status;
        Project project = new Project();
        project.setName(name);
        project.setClientId(clientId);
        project.setStatus(projectStatus);
        Project saved = repository.save(project);
        String clientName = jdbc.queryForObject(
                "SELECT name FROM clients WHERE id = ?", String.class, clientId);
        return new ProjectView(saved.getId(), saved.getName(), clientId, clientName,
                saved.getStatus());
    }

    @Transactional
    public List<ProjectView> delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("A project id is required to delete");
        }
        repository.deleteById(id);
        audit.record("PROJECT_DELETED id=" + id);
        return list();
    }

    /**
     * Archive a project: keep the row but flag it so it drops off the default lists (the main project
     * list and the client's page). Records an audit entry so the action can be checked later, and
     * returns the projects that remain visible.
     */
    @Transactional
    public List<ProjectView> archive(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("A project id is required to archive");
        }
        jdbc.update("UPDATE projects SET archived = TRUE WHERE id = ?", id);
        audit.record("PROJECT_ARCHIVED id=" + id);
        return list();
    }
}
