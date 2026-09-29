// Manifest-based routing (shared surface). A feature is a NEW file at features/<name>/feature.tsx
// that exports a `feature: Feature`; it is discovered here via import.meta.glob, so adding a page is
// a new file — never an edit to this router (DESIGN.md §8).
import type { ComponentType } from 'react';

export type Feature = {
  id: string; // section id -> nav-<id> data-testid
  label: string; // nav link text
  Page: ComponentType;
};

const modules = import.meta.glob<{ feature: Feature }>('../features/*/feature.tsx', { eager: true });

export const features: Feature[] = Object.values(modules)
  .map((m) => m.feature)
  .filter((f): f is Feature => Boolean(f))
  .sort((a, b) => a.label.localeCompare(b.label));
