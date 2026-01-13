-- Add parent_id for comment hierarchy (self-referential)
ALTER TABLE posts ADD COLUMN parent_id BINARY(16) NULL;

-- Foreign key with SET NULL on delete (for admin hard-delete scenarios)
ALTER TABLE posts ADD CONSTRAINT fk_posts_parent
    FOREIGN KEY (parent_id) REFERENCES posts(id) ON DELETE SET NULL;

-- Add soft delete flag (posts are never truly deleted, just deactivated)
ALTER TABLE posts ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

-- Indexes for efficient queries
CREATE INDEX idx_posts_parent_id ON posts(parent_id);
CREATE INDEX idx_posts_active ON posts(active);
CREATE INDEX idx_posts_parent_created ON posts(parent_id, created_at DESC);
