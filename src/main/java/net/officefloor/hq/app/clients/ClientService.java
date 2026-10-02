package net.officefloor.hq.app.clients;

import java.util.List;
import java.util.regex.Pattern;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Business logic for clients. Each call runs in its own transaction. */
@Service
public class ClientService {

    // A client must carry a proper email address (mirrors the front-end guard).
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final ClientRepository repository;
    private final JdbcTemplate jdbc;

    public ClientService(ClientRepository repository, JdbcTemplate jdbc) {
        this.repository = repository;
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public List<Client> list() {
        return repository.findAllByOrderByIdAsc();
    }

    /** Clients whose name contains {@code query} (case-insensitive); all clients when blank. */
    @Transactional(readOnly = true)
    public List<Client> search(String query) {
        if (query == null || query.isBlank()) {
            return list();
        }
        return repository.findByNameContainingIgnoreCaseOrderByIdAsc(query.trim());
    }

    /**
     * The projects done for a client, oldest first. Reads the {@code projects} table via SQL so the
     * clients feature stays self-contained and does not import the projects feature's Java types.
     */
    @Transactional(readOnly = true)
    public List<ClientProjectView> projectsFor(Long clientId) {
        return jdbc.query(
                "SELECT id, name FROM projects WHERE client_id = ? ORDER BY id ASC",
                (rs, i) -> new ClientProjectView(rs.getLong("id"), rs.getString("name")),
                clientId);
    }

    @Transactional
    public Client create(String name, String email) {
        if (email == null || !EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("A client requires a valid email address");
        }
        Client client = new Client();
        client.setName(name);
        client.setEmail(email);
        return repository.save(client);
    }
}
