-- Create users table for Motivise study blogging platform
CREATE TABLE users (
                       id BINARY(16) NOT NULL PRIMARY KEY,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       country_code CHAR(2) NOT NULL,
                       profile_image_url VARCHAR(500) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NULL
);



-- Insert sample users for Motivise study blogging platform
INSERT INTO users (id, email, username, password_hash, country_code, profile_image_url, role, created_at)
VALUES
    (UUID_TO_BIN('a8f23b8e-2a40-4f1a-9e7d-21a4c7c89d12'),
     'anna.schmidt@example.com',
     'study_anna',
     'Password123!',
     'AT',
     'https://example.com/images/profile1.png',
     'USER',
     NOW()),

    (UUID_TO_BIN('b6d5e90c-3e55-4c5f-bc1d-6720a2c841a9'),
     'max.meier@example.com',
     'maxlearns',
     'Password123!',
     'DE',
     'https://example.com/images/profile2.png',
     'USER',
     NOW()),

    (UUID_TO_BIN('c27f9b6b-6c8d-4d62-9e3a-1e8d87c7adf0'),
     'admin@motivise.app',
     'motadmin',
     'AdminPass456!',
     'CH',
     'https://example.com/images/admin.png',
     'ADMIN',
     NOW());
