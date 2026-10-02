import type { AppRoute } from '../../router/routes';
import { AllInvoicesPage } from './AllInvoicesPage';

export const route: AppRoute = {
  id: 'invoices',
  label: 'Invoices',
  Component: AllInvoicesPage,
};
