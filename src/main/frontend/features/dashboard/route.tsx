import type { FeatureRoute } from '../../router/routes';
import { DashboardPage } from './DashboardPage';

export const route: FeatureRoute = {
  section: 'dashboard',
  label: 'Dashboard',
  order: 0,
  Page: DashboardPage,
};
