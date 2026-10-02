package net.officefloor.hq.app.clients;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Business logic for clients. Each call runs in its own transaction. */
@Service
public class ClientService {

    private final ClientRepository repository;

    public ClientService(ClientRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Client> list() {
        return repository.findAllByOrderByIdAsc();
    }

    @Transactional
    public Client create(String name, String email) {
        Client client = new Client();
        client.setName(name);
        client.setEmail(email);
        return repository.save(client);
    }
}
