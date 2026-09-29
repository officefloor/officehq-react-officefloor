import type { FeatureRoute } from '../../router/routes';
import { ClientsPage } from './ClientsPage';

export const route: FeatureRoute = {
  section: 'clients',
  label: 'Clients',
  order: 1,
  Page: ClientsPage,
};
