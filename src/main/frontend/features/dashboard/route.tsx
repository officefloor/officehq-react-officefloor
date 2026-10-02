import type { AppRoute } from '../../router/routes';
import { DashboardPage } from './DashboardPage';

export const route: AppRoute = {
  id: 'dashboard',
  label: 'Dashboard',
  Component: DashboardPage,
};
