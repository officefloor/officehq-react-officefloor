// Shared shape of the projects feature (CLAUDE.md — a feature keeps its code together). The list
// and the "add a job" form both work with these types, so they live in one place.
export type ProjectStatus = 'ACTIVE' | 'ON_HOLD' | 'FINISHED';

export type Project = {
  id: number;
  name: string;
  clientId: number;
  clientName: string;
  archived: boolean;
  status: ProjectStatus;
  code: string;
};

export type Client = { id: number; name: string };

export const STATUSES: ProjectStatus[] = ['ACTIVE', 'ON_HOLD', 'FINISHED'];
