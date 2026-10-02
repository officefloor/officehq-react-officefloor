// Shared surface: the route registry. Features self-register by exporting a `route` from
// features/<name>/route.tsx — so adding a page is a NEW file under features/, never an edit here
// (DESIGN.md §8). This file only discovers and orders what the features declare.
import type { ComponentType } from 'react';

export type AppRoute = {
  id: string; // section id; drives the nav data-testid: nav-<id>
  label: string;
  Component: ComponentType;
};

const modules = import.meta.glob('../features/*/route.tsx', { eager: true });

export const routes: AppRoute[] = Object.values(modules)
  .map((m) => (m as { route?: AppRoute }).route)
  .filter((r): r is AppRoute => Boolean(r))
  .sort((a, b) => a.label.localeCompare(b.label));
