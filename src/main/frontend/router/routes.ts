// Shared routing surface (config.yaml -> app.shared_surfaces.frontend).
// A page is registered by its feature, not listed here: every features/<name>/route.tsx exports a
// `route` data declaration, and this module auto-discovers them. Adding a page = a NEW feature route
// module, never an edit to this file (DESIGN.md §8).
import type { ComponentType } from 'react';

export type AppRoute = {
  /** Section id. Drives the nav link testid (`nav-<id>`) and selects the active page. */
  id: string;
  /** Human label shown in the nav. */
  label: string;
  /** The screen component for this section. */
  component: ComponentType;
};

// Eager glob so each feature's route declaration is collected at module load.
const modules = import.meta.glob<{ route: AppRoute }>('../features/*/route.tsx', { eager: true });

export const routes: AppRoute[] = Object.values(modules)
  .map((m) => m.route)
  .filter((r): r is AppRoute => Boolean(r));
