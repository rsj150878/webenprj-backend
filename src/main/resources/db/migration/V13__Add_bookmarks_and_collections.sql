-- Create table for bookmark collections (user-owned folders)
CREATE TABLE bookmark_collections (
    id BINARY(16) NOT NULL PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    color VARCHAR(7),
    icon_name VARCHAR(50),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_collections_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_collections_name UNIQUE (user_id, name)
);

-- Create table for post bookmarks
CREATE TABLE post_bookmarks (
    id BINARY(16) NOT NULL PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    post_id BINARY(16) NOT NULL,
    collection_id BINARY(16),
    notes VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookmarks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookmarks_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookmarks_collection FOREIGN KEY (collection_id) REFERENCES bookmark_collections(id) ON DELETE SET NULL,
    CONSTRAINT uq_bookmarks UNIQUE (user_id, post_id)
);

-- Indexes to accelerate lookups
CREATE INDEX idx_bookmarks_user ON post_bookmarks(user_id);
CREATE INDEX idx_bookmarks_post ON post_bookmarks(post_id);
CREATE INDEX idx_bookmarks_collection ON post_bookmarks(collection_id);
CREATE INDEX idx_bookmarks_created_at ON post_bookmarks(created_at DESC);
CREATE INDEX idx_collections_user ON bookmark_collections(user_id);
