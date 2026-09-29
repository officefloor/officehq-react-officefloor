import type { FeatureRoute } from '../../router/routes';
import { AllInvoicesPage } from './AllInvoicesPage';

export const route: FeatureRoute = {
  section: 'invoices',
  label: 'Invoices',
  order: 3,
  Page: AllInvoicesPage,
};
