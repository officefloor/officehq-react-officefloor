// Shared surface (config.yaml -> app.shared_surfaces.frontend): the ADDITIVE routing mechanism.
// Feature pages register by dropping a `route.tsx` under features/<name>/ — they are auto-discovered
// here via import.meta.glob, so adding a page is a NEW file, never an edit to this file (DESIGN.md
// §8). Each feature module exports a `route: FeatureRoute`.
import type { ComponentType } from 'react';

export type FeatureRoute = {
  section: string; // identity + nav-<section> testid
  label: string; // nav link text
  order?: number; // nav ordering (lower first)
  Page: ComponentType;
};

const modules = import.meta.glob('../features/*/route.tsx', { eager: true }) as Record<
  string,
  { route: FeatureRoute }
>;

export const routes: FeatureRoute[] = Object.values(modules)
  .map((m) => m.route)
  .sort((a, b) => (a.order ?? 0) - (b.order ?? 0) || a.label.localeCompare(b.label));
