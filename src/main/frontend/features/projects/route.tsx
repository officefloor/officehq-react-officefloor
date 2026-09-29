import type { FeatureRoute } from '../../router/routes';
import { ProjectsPage } from './ProjectsPage';

export const route: FeatureRoute = {
  section: 'projects',
  label: 'Jobs',
  order: 2,
  Page: ProjectsPage,
};
