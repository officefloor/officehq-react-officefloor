package net.officefloor.hq.app;

import net.officefloor.web.ObjectResponse;

/** GET /api/dashboard — counts of clients and projects plus the outstanding (unpaid) total. */
public class DashboardGetLogic {

    public void service(DashboardRepository repository, ObjectResponse<DashboardSummary> response) {
        response.send(repository.summary());
    }
}
