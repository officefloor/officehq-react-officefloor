import type { FeatureRoute } from '../../router/routes';
import { SearchPage } from './SearchPage';

// Order below the dashboard so the one global search box is the landing page at '/'.
export const route: FeatureRoute = {
  section: 'search',
  label: 'Search',
  order: -1,
  Page: SearchPage,
};
