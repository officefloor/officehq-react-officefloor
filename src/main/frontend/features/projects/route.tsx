import type { AppRoute } from '../../router/routes';
import { ProjectsPage } from './ProjectsPage';

export const route: AppRoute = {
  id: 'projects',
  label: 'Jobs',
  Component: ProjectsPage,
};
