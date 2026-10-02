import type { AppRoute } from '../../router/routes';
import { ClientsPage } from './ClientsPage';

export const route: AppRoute = {
  id: 'clients',
  label: 'Clients',
  Component: ClientsPage,
};
