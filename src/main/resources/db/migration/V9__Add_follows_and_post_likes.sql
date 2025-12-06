-- Create table for user follows (subscriptions)
CREATE TABLE follows (
    id BINARY(16) NOT NULL PRIMARY KEY,
    follower_id BINARY(16) NOT NULL,
    followed_id BINARY(16) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_follows_followed FOREIGN KEY (followed_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_follows UNIQUE (follower_id, followed_id),
    CONSTRAINT chk_not_self_follow CHECK (follower_id <> followed_id)
);

-- Create table for post likes
CREATE TABLE post_likes (
    id BINARY(16) NOT NULL PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    post_id BINARY(16) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_postlikes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_postlikes_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT uq_postlikes UNIQUE (user_id, post_id)
);

-- Indexes to accelerate lookups
CREATE INDEX idx_follows_follower ON follows(follower_id);
CREATE INDEX idx_follows_followed ON follows(followed_id);
CREATE INDEX idx_postlikes_post ON post_likes(post_id);
CREATE INDEX idx_postlikes_user ON post_likes(user_id);
