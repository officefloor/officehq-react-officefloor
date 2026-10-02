import type { AppRoute } from '../../router/routes';
import { ClientsPage } from './ClientsPage';

// Feature route declaration — pure data, auto-discovered by router/routes.ts.
export const route: AppRoute = {
  id: 'clients',
  label: 'Clients',
  component: ClientsPage,
};
