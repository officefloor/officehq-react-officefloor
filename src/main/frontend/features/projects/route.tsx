import type { AppRoute } from '../../router/routes';
import { ProjectsPage } from './ProjectsPage';

// Feature route declaration — pure data, auto-discovered by router/routes.ts.
export const route: AppRoute = {
  id: 'projects',
  label: 'Projects',
  component: ProjectsPage,
};
