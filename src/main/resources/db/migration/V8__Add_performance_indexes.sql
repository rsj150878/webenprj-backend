-- V8__Add_performance_indexes.sql
-- Add indexes to improve query performance on frequently accessed columns

-- Index on posts.user_id for faster user post lookups
CREATE INDEX idx_posts_user_id ON posts(user_id);

-- Index on posts.created_at for faster date-based ordering and filtering
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);

-- Index on posts.subject for faster subject-based searches
CREATE INDEX idx_posts_subject ON posts(subject);

-- Index on users.email for faster email lookups during authentication
CREATE INDEX idx_users_email ON users(email);

-- Index on users.username for faster username lookups during authentication
CREATE INDEX idx_users_username ON users(username);

-- Index on users.active for faster filtering of active/inactive users
CREATE INDEX idx_users_active ON users(active);

-- Composite index on media for faster user media lookups
CREATE INDEX idx_media_user_id_created_at ON media(user_id, created_at DESC);
