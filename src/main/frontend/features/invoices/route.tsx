import type { AppRoute } from '../../router/routes';
import { AllInvoicesPage } from './AllInvoicesPage';

// Feature route declaration — pure data, auto-discovered by router/routes.ts.
export const route: AppRoute = {
  id: 'invoices',
  label: 'Invoices',
  component: AllInvoicesPage,
};
