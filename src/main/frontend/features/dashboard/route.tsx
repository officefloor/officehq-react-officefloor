import type { AppRoute } from '../../router/routes';
import { DashboardPage } from './DashboardPage';

// Feature route declaration — pure data, auto-discovered by router/routes.ts.
export const route: AppRoute = {
  id: 'dashboard',
  label: 'Dashboard',
  component: DashboardPage,
};
