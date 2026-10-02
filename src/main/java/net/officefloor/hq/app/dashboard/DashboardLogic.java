package net.officefloor.hq.app.dashboard;

import net.officefloor.web.ObjectResponse;

/** GET /api/dashboard — headline counts and the outstanding (unpaid) money total for the home screen. */
public class DashboardLogic {

    public void service(DashboardService dashboard, ObjectResponse<DashboardView> response) {
        response.send(dashboard.summary());
    }
}
