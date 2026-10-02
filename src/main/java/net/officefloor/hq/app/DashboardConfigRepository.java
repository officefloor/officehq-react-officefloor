package net.officefloor.hq.app;

import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link DashboardConfig}. A Spring bean, injected into the dashboard logic. */
public interface DashboardConfigRepository extends JpaRepository<DashboardConfig, Long> {
}
