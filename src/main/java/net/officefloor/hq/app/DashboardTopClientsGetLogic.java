package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/dashboard/top-clients — the home screen's top five clients ranked by how much they still
 * owe (highest first). Read-only; the dashboard feature owns this alongside its summary.
 */
public class DashboardTopClientsGetLogic {

    public void service(DashboardRepository repository, ObjectResponse<List<TopClient>> response) {
        response.send(repository.topClients(5));
    }
}
