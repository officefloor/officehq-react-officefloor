package net.officefloor.hq.app;

import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Per-spec data setup for the harness (DESIGN.md §9). Profile-guarded so it exists ONLY under the
 * harness launch (bin/start sets spring.profiles.active=harness) — never in a real deploy. This is
 * APP CODE and EVOLVES with the schema (NOT pinned); a change that breaks a prior spec's seed is a
 * seed-path regression. Tests call these to ARRANGE data; they ASSERT only through the UI.
 */
@Profile("harness")
@RestController
@RequestMapping("/__test__")
public class TestSupportController {

    private final Audit audit;
    private final JdbcTemplate jdbc;

    public TestSupportController(Audit audit, JdbcTemplate jdbc) {
        this.audit = audit;
        this.jdbc = jdbc;
    }

    /** Truncate all domain tables and clear the audit file so each spec starts clean. */
    @PostMapping("/reset")
    public void reset() {
        audit.clear();
        // Disable FK checks so a referenced parent (clients) can be truncated alongside its child.
        jdbc.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbc.execute("TRUNCATE TABLE projects RESTART IDENTITY");
        jdbc.execute("TRUNCATE TABLE clients RESTART IDENTITY");
        jdbc.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    /** Insert the fixture a spec needs; the payload shape evolves with the schema. */
    @PostMapping("/seed")
    @SuppressWarnings("unchecked")
    public void seed(@RequestBody Map<String, Object> fixture) {
        List<Map<String, Object>> clients =
                (List<Map<String, Object>>) fixture.getOrDefault("clients", List.of());
        long maxClientId = 0;
        for (Map<String, Object> client : clients) {
            long id = ((Number) client.get("id")).longValue();
            // Seed with the fixture's explicit id (JdbcTemplate, not JPA) so specs can assert by id.
            jdbc.update("INSERT INTO clients (id, name, email) VALUES (?, ?, ?)",
                    id, client.get("name"), client.get("email"));
            maxClientId = Math.max(maxClientId, id);
        }
        if (maxClientId > 0) {
            // Advance the identity so app-generated ids don't collide with seeded rows.
            jdbc.execute("ALTER TABLE clients ALTER COLUMN id RESTART WITH " + (maxClientId + 1));
        }

        List<Map<String, Object>> projects =
                (List<Map<String, Object>>) fixture.getOrDefault("projects", List.of());
        long maxProjectId = 0;
        for (Map<String, Object> project : projects) {
            long id = ((Number) project.get("id")).longValue();
            long clientId = ((Number) project.get("clientId")).longValue();
            jdbc.update("INSERT INTO projects (id, name, client_id) VALUES (?, ?, ?)",
                    id, project.get("name"), clientId);
            maxProjectId = Math.max(maxProjectId, id);
        }
        if (maxProjectId > 0) {
            jdbc.execute("ALTER TABLE projects ALTER COLUMN id RESTART WITH " + (maxProjectId + 1));
        }
    }
}
