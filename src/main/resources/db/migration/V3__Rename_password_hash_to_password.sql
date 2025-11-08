-- Rename password_hash column to password in users table
ALTER TABLE users
    CHANGE COLUMN password_hash password VARCHAR(255) NOT NULL;
