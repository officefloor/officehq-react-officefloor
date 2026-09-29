-- Notes are now written on invoices as well as projects, so both the project and invoice note
-- lookups filter by (target_type, target_id). Index that pair to keep the newest-first lookup cheap.
-- Additive: a new versioned migration, no change to the notes table's shape.
CREATE INDEX idx_notes_target ON notes (target_type, target_id);
