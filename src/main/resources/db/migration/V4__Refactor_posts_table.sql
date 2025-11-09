-- Drop old posts table (BIGINT id, title, no user_id)
DROP TABLE IF EXISTS posts;

-- Recreate posts table matching the new Post entity
CREATE TABLE posts (
                       id BINARY(16) NOT NULL PRIMARY KEY,
                       subject VARCHAR(30) NOT NULL,
                       content VARCHAR(500) NOT NULL,
                       image_url VARCHAR(500),
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NULL,
                       user_id BINARY(16) NOT NULL,
                       CONSTRAINT fk_posts_user
                           FOREIGN KEY (user_id) REFERENCES users(id)
                               ON DELETE CASCADE
);


-- ===========================================
-- DEMO DATA (adjust UUIDs to existing user IDs)
-- ===========================================

